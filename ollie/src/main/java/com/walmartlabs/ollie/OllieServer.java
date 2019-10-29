package com.walmartlabs.ollie;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 Takari
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.list;
import static java.util.Comparator.naturalOrder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.walmartlabs.ollie.guice.InjectorBuilder;
import com.walmartlabs.ollie.guice.OllieServerComponents;
import com.walmartlabs.ollie.guice.OllieShutdownManager;
import com.walmartlabs.ollie.lifecycle.Lifecycle;
import com.walmartlabs.ollie.lifecycle.LifecycleRepository;
import com.walmartlabs.ollie.model.FilterDefinition;
import com.walmartlabs.ollie.model.ServletDefinition;
import com.walmartlabs.ollie.model.StaticResourceDefinition;
import io.airlift.security.pem.PemReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// http://stackoverflow.com/questions/20043097/jetty-9-embedded-adding-handlers-during-runtime

public class OllieServer {

  public static final String DATABASE_CHANGELOG_RESOURCE = "liquibase.xml";
  public static final String DATABASE_CHANGELOG_TABLE_NAME = "databasechangelog";
  public static final String DATABASE_CHANGELOG_LOCK_TABLE_NAME = "databasechangeloglock";

  private static Logger logger = LoggerFactory.getLogger(OllieServer.class);
  protected final Server server;
  private final Optional<ZonedDateTime> certificateExpiration;

  // Need to share for testing
  OllieShutdownManager shutdownManager;
  OllieServerComponents components;

  public OllieServer(OllieServerBuilder builder) {
    this.server = build(builder);
    this.certificateExpiration = loadAllX509Certificates(builder).stream().map(X509Certificate::getNotAfter).min(naturalOrder())
        .map(date -> ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
  }

  public List<Lifecycle> tasks() {
    return components.tasks();
  }

  public Server build(OllieServerBuilder builder) {
    logger.info("Constructing Jetty server...");
    logger.info("maxThreads = {}", builder.maxThreads);
    logger.info("minThreads = {}", builder.minThreads);
    logger.info("port = {}", builder.port);
    QueuedThreadPool threadPool = new QueuedThreadPool(builder.maxThreads);
    threadPool.setMinThreads(builder.minThreads);
    threadPool.setIdleTimeout(builder.threadIdleTimeout);
    threadPool.setName("http-worker");
    Server server = new Server(threadPool);

    if (builder.jmxEnabled) {
      MBeanContainer mbeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
      server.addBean(mbeanContainer);
    }

    HttpConfiguration baseHttpConfiguration = new HttpConfiguration();
    baseHttpConfiguration.setSendServerVersion(false);
    baseHttpConfiguration.setSendXPoweredBy(false);

    ServerConnector httpsConnector;
    if (builder.sslEnabled) {
      logger.info("HTTPS connector enabled");
      HttpConfiguration httpsConfiguration = new HttpConfiguration(baseHttpConfiguration);
      httpsConfiguration.setSecureScheme("https");
      httpsConfiguration.setSecurePort(builder.port());
      httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

      SslContextFactory sslContextFactory = new SslContextFactory();
      Optional<KeyStore> pemKeyStore = tryLoadPemKeyStore(builder);
      if (pemKeyStore.isPresent()) {
        sslContextFactory.setKeyStore(pemKeyStore.get());
        sslContextFactory.setKeyStorePassword("");
      } else {
        throw new RuntimeException("SSL has been configured but the keystore file cannot be loaded: " + builder.keystorePath);
      }
      if (builder.truststorePath != null) {
        Optional<KeyStore> pemTrustStore = tryLoadPemTrustStore(builder);
        if (pemTrustStore.isPresent()) {
          sslContextFactory.setTrustStore(pemTrustStore.get());
          sslContextFactory.setTrustStorePassword("");
        } else {
          throw new RuntimeException("SSL has been configured but the truststore file cannot be loaded: " + builder.keystorePath);
        }
      }

      // https://github.com/eclipse/jetty.project/issues/3466
      sslContextFactory.setEndpointIdentificationAlgorithm(null);
      sslContextFactory.setSecureRandomAlgorithm(builder.secureRandomAlgorithm);
      sslContextFactory.setWantClientAuth(true);
      sslContextFactory.setSslSessionTimeout((int) builder.sslSessionTimeout);
      sslContextFactory.setSslSessionCacheSize(builder.sslSessionCacheSize);
      SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");

      httpsConnector = new ServerConnector(server, sslConnectionFactory, new HttpConnectionFactory(httpsConfiguration));
      httpsConnector.setName("https");
      httpsConnector.setPort(builder.port);

      server.addConnector(httpsConnector);
    }

    ServerConnector httpConnector;
    if (!builder.sslEnabled()) {
      logger.info("HTTP connector enabled.");
      httpConnector = new ServerConnector(server, new HttpConnectionFactory(baseHttpConfiguration));
      httpConnector.setName("http");
      httpConnector.setPort(builder.port);
      server.addConnector(httpConnector);
    }

    ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

    if (builder.staticContentDefinitions.size() > 0) {
      logger.info("Setting up static resources handlers.");
      for (StaticResourceDefinition definition : builder.staticContentDefinitions) {
        String[] welcomeFiles = definition.getWelcomeFiles().toArray(new String[0]);
        File fileResourcePath = new File(definition.getResource());
        if (fileResourcePath.exists()) {
          contextHandlerCollection.addHandler(fileResourceHandler(definition));
        } else {
          contextHandlerCollection.addHandler(classpathResourceHandler(definition.getPath(), definition.getResource(), welcomeFiles));
        }
      }
    }

    int options = ServletContextHandler.NO_SESSIONS;
    if (builder.sessionsEnabled) {
      logger.info("Enabling session support.");
      options |= ServletContextHandler.SESSIONS;
    }

    ServletContextHandler applicationContext = new ServletContextHandler(options);

    if (builder.sessionMaxInactiveInterval > 0) {
      logger.info("Setting max inactive interval for sessions: {}s.", builder.sessionMaxInactiveInterval);
      SessionHandler sessionHandler = applicationContext.getSessionHandler();
      sessionHandler.setMaxInactiveInterval(builder.sessionMaxInactiveInterval);
    }

    //
    // Set up the Guice Injector, create all the bindings and scan for annotations
    //
    ServletContext servletContext = applicationContext.getServletContext();
    ExecutorService executor = Executors.newCachedThreadPool();
    LifecycleRepository taskRepository = new LifecycleRepository(executor);
    shutdownManager = new OllieShutdownManager(taskRepository, builder.shutdownListeners);
    InjectorBuilder injectorBuilder = new InjectorBuilder(builder, executor, taskRepository, servletContext, shutdownManager);
    Injector injector = injectorBuilder.injector();

    // Build up a list of all the configured contexts and make sure they are not clobbered by any  @WebServlet contextPaths
    Map<String,String> configuredContextPaths = Maps.newHashMap();
    configuredContextPaths.put(builder.api(), "Ollie REST System");
    configuredContextPaths.put(builder.docs(), "Ollie REST System");
    for(ServletDefinition servletDefinition : builder.servletDefinitions) {
      configuredContextPaths.put(servletDefinition.getPattern(), servletDefinition.getServlet() != null ? servletDefinition.getServlet().toString() : servletDefinition.getServletClass().getName());
    }

    //
    // At this point the Injector is created and all the Module.configure() methods have executed
    //
    for (Map.Entry<String, TypeLiteral<? extends HttpServlet>> entry : injectorBuilder.webServlets().entrySet()) {
      String contextPath = entry.getKey();
      TypeLiteral<? extends HttpServlet> typeLiteral = entry.getValue();
      Servlet servlet = injector.getInstance(Key.get(typeLiteral));
      if(configuredContextPaths.containsKey(contextPath)) {
        // The @WebServet class com.walmartlabs.ollie.app.TestWebServlet uses the contextPath '/testservlet' which is already in use by com.walmartlabs.ollie.app.TestServlet.
        throw new RuntimeException(
                String.format("The @WebServet %s uses the contextPath '%s' which is already in use by %s.", servlet.getClass(), contextPath, configuredContextPaths.get(contextPath)));
      }
      ServletHolder servletHolder = new ServletHolder(servlet);
      applicationContext.addServlet(servletHolder, contextPath);
      // This is the way to have a servlet respond to multiple context paths to support @WebServlets properly
      // and deal with multiple patterns for Siesta that Concord is currently using
      //applicationContext.getServletHandler().addServletWithMapping(servletHolder, contextPath);
    }

    ServletContextListener contextListener = null;

    components = injector.getInstance(OllieServerComponents.class);
    // Trigger the execution of the tasks. Hopefully there is a more elegant way in Guice to trigger
    // the scanning an discovery of instances we want managed by a ProvisionerListener
    for (Lifecycle task : components.tasks()) {
      task.toString();
    }

    Set<SessionCookieOptions> opts = builder.sessionCookieOptions();
    SessionCookieConfig cfg = servletContext.getSessionCookieConfig();
    if (opts.contains(SessionCookieOptions.SECURE)) {
      cfg.setSecure(true);
    }
    if (opts.contains(SessionCookieOptions.HTTP_ONLY)) {
      cfg.setHttpOnly(true);
    }

    if (contextListener != null) {
      logger.info("Adding context listener {}.", contextListener.getClass().getName());
      applicationContext.addEventListener(contextListener);
    }

    if (builder.filterDefintions.size() > 0) {
      logger.info("Setting up servlet filters.");
      for (FilterDefinition filterDefinition : builder.filterDefintions) {
        String[] patterns = filterDefinition.getPatterns();
        for (String pattern : patterns) {
          logger.info("Filter {} -> {}.", pattern, filterDefinition.getFilterClass());
          applicationContext.addFilter(new FilterHolder(filterDefinition.getFilterClass()), pattern, null);
        }
      }
    }

    if (builder.servletDefinitions.size() > 0) {
      logger.info("Setting up servlets.");
      for (ServletDefinition servletDefinition : builder.servletDefinitions) {
        if (servletDefinition.getWar() != null) {
          WebAppContext webapp = new WebAppContext(applicationContext, servletDefinition.getWar().getAbsolutePath(), servletDefinition.getPattern());
          if (servletDefinition.getSecurityHandler() != null) {
            webapp.setSecurityHandler(servletDefinition.getSecurityHandler());
          }
        } else if (servletDefinition.getServlet() != null) {
          // Use a Servlet instance
          ServletHolder servletHolder = new ServletHolder(servletDefinition.getServlet());
          applicationContext.addServlet(servletHolder, servletDefinition.getPattern());
        } else {
          // Use a Servlet class
          ServletHolder servletHolder = new ServletHolder(servletDefinition.getServletClass());
          servletHolder.setInitParameters(servletDefinition.getParameters());
          applicationContext.addServlet(servletHolder, servletDefinition.getPattern());
        }
      }
    }

    contextHandlerCollection.addHandler(applicationContext);
    StatisticsHandler statisticsHandler = new StatisticsHandler();
    statisticsHandler.setHandler(contextHandlerCollection);
    server.setHandler(statisticsHandler);

    RequestLog requestLog = builder.requestLog();
    if (requestLog != null) {
      server.setRequestLog(requestLog);
    }

    return server;
  }

  private ContextHandler fileResourceHandler(StaticResourceDefinition definition) {
    String[] welcomeFiles = definition.getWelcomeFiles().toArray(new String[0]);
    ContextHandler contextHandler = new ContextHandler();
    ResourceHandler resourceHandler = new ResourceHandler();
    File css = new File(definition.getResource(), "jetty-dir.css");
    if (css.exists()) {
      resourceHandler.setStylesheet(css.getAbsolutePath());
    }
    resourceHandler.setDirectoriesListed(definition.isListing());
    resourceHandler.setWelcomeFiles(welcomeFiles);
    contextHandler.setBaseResource(Resource.newResource(new File(definition.getResource())));
    contextHandler.setHandler(resourceHandler);
    contextHandler.setContextPath(definition.getPath());
    return contextHandler;
  }

  private ContextHandler classpathResourceHandler(String context, String path, String[] welcomeFiles) {
    ContextHandler contextHandler = new ContextHandler();
    ResourceHandler resourceHandler = new ResourceHandler();
    resourceHandler.setDirectoriesListed(false);
    resourceHandler.setWelcomeFiles(welcomeFiles);
    Resource resourcesClasspath = Resource.newClassPathResource(path);
    contextHandler.setBaseResource(resourcesClasspath);
    contextHandler.setHandler(resourceHandler);
    contextHandler.setContextPath(context);
    return contextHandler;
  }

  private static Optional<KeyStore> tryLoadPemKeyStore(OllieServerBuilder config) {
    File keyStoreFile = new File(config.keystorePath);
    try {
      if (!PemReader.isPem(keyStoreFile)) {
        return Optional.empty();
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading key store file: " + keyStoreFile, e);
    }

    try {
      return Optional.of(PemReader.loadKeyStore(keyStoreFile, keyStoreFile, Optional.ofNullable(config.keystorePassword)));
    } catch (IOException | GeneralSecurityException e) {
      throw new IllegalArgumentException("Error loading PEM key store: " + keyStoreFile, e);
    }
  }

  private static Optional<KeyStore> tryLoadPemTrustStore(OllieServerBuilder config) {
    File trustStoreFile = new File(config.truststorePath);
    try {
      if (!PemReader.isPem(trustStoreFile)) {
        return Optional.empty();
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Error reading trust store file: " + trustStoreFile, e);
    }

    try {
      if (PemReader.readCertificateChain(trustStoreFile).isEmpty()) {
        throw new IllegalArgumentException("PEM trust store file does not contain any certificates: " + trustStoreFile);
      }
      return Optional.of(PemReader.loadTrustStore(trustStoreFile));
    } catch (IOException | GeneralSecurityException e) {
      throw new IllegalArgumentException("Error loading PEM trust store: " + trustStoreFile, e);
    }
  }

  private static Set<X509Certificate> loadAllX509Certificates(OllieServerBuilder config) {
    ImmutableSet.Builder<X509Certificate> certificates = ImmutableSet.builder();
    if (config.sslEnabled()) {
      try (InputStream keystoreInputStream = new FileInputStream(config.keystorePath)) {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(keystoreInputStream, config.keystorePassword.toCharArray());

        for (String alias : list(keystore.aliases())) {
          try {
            Certificate certificate = keystore.getCertificate(alias);
            if (certificate instanceof X509Certificate) {
              certificates.add((X509Certificate) certificate);
            }
          } catch (KeyStoreException ignored) {
          }
        }
      } catch (Exception ignored) {
      }
    }
    return certificates.build();
  }

  public Long getDaysUntilCertificateExpiration() {
    return certificateExpiration.map(date -> ZonedDateTime.now().until(date, DAYS)).orElse(null);
  }

  public void start() {
    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void startAndWait() {
    try {
      server.start();
      server.join();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized void stop() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int port() {
    return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
  }

  public static OllieServerBuilder builder() {
    return new OllieServerBuilder();
  }

  public OllieShutdownManager shutdownManager() {
    return shutdownManager;
  }
}
