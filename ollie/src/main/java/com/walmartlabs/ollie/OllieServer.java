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
import java.util.Optional;
import java.util.Set;

import com.walmartlabs.ollie.guice.OllieServerComponents;
import com.walmartlabs.ollie.lifecycle.Lifecycle;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
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

import com.google.common.collect.ImmutableSet;
import com.walmartlabs.ollie.model.FilterDefinition;
import com.walmartlabs.ollie.model.ServletDefinition;
import com.walmartlabs.ollie.model.StaticResourceDefinition;

import io.airlift.security.pem.PemReader;

import javax.inject.Inject;

// http://stackoverflow.com/questions/20043097/jetty-9-embedded-adding-handlers-during-runtime

public class OllieServer {

  public static final String DATABASE_CHANGELOG_RESOURCE = "liquibase.xml";
  public static final String DATABASE_CHANGELOG_TABLE_NAME = "databasechangelog";
  public static final String DATABASE_CHANGELOG_LOCK_TABLE_NAME = "databasechangeloglock";

  private static Logger logger = LoggerFactory.getLogger(OllieServer.class);
  protected final Server server;
  private final Optional<ZonedDateTime> certificateExpiration;
  private final OllieServerComponents components;

  @Inject
  public OllieServer(OllieServerBuilder builder, OllieServerComponents components) {
    this.server = build(builder);
    this.components = components;
    this.certificateExpiration = loadAllX509Certificates(builder).stream()
      .map(X509Certificate::getNotAfter)
      .min(naturalOrder())
      .map(date -> ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));

    // Trigger the execution of the tasks. Hopefully there is a more elegant way in Guice to trigger
    // the scanning an discovery of instances we want managed by a ProvisionerListener
    for(Lifecycle task : components.tasks()) {
      task.toString();
    }
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
    // threadPool.setIdleTimeout(Ints.checkedCast(config.getThreadMaxIdleTime().toMillis()));
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

      List<String> includedCipherSuites = builder.includedCipherSuites;
      sslContextFactory.setIncludeCipherSuites(includedCipherSuites.toArray(new String[includedCipherSuites.size()]));
      List<String> excludedCipherSuites = builder.excludedCipherSuites;
      sslContextFactory.setExcludeCipherSuites(excludedCipherSuites.toArray(new String[excludedCipherSuites.size()]));
      sslContextFactory.setSecureRandomAlgorithm(builder.secureRandomAlgorithm);
      sslContextFactory.setWantClientAuth(true);
      sslContextFactory.setSslSessionTimeout((int) builder.sslSessionTimeout);
      sslContextFactory.setSslSessionCacheSize(builder.sslSessionCacheSize);
      SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");

      httpsConnector = new ServerConnector(server, sslConnectionFactory, new HttpConnectionFactory(httpsConfiguration));
      httpsConnector.setName("https");
      httpsConnector.setPort(builder.port);
      //httpsConnector.setIdleTimeout(config.getNetworkMaxIdleTime().toMillis());
      //httpsConnector.setHost(nodeInfo.getBindIp().getHostAddress());
      //httpsConnector.setAcceptQueueSize(config.getHttpAcceptQueueSize());

      server.addConnector(httpsConnector);
    }

    ServerConnector httpConnector;
    if (!builder.sslEnabled()) {
      logger.info("HTTP connector enabled.");
      httpConnector = new ServerConnector(server, new HttpConnectionFactory(baseHttpConfiguration));
      httpConnector.setName("http");
      httpConnector.setPort(builder.port);
      // httpConnector.setIdleTimeout(config.getNetworkMaxIdleTime().toMillis());
      // httpConnector.setHost(nodeInfo.getBindIp().getHostAddress());
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

    if (builder.contextListener != null) {
      logger.info("Adding context listener {}.", builder.contextListener.getClass().getName());
      applicationContext.addEventListener(builder.contextListener);
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
          ServletHolder servletHolder = new ServletHolder(servletDefinition.getServlet());
          applicationContext.addServlet(servletHolder, servletDefinition.getPattern());
        } else {
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
    return certificateExpiration.map(date -> ZonedDateTime.now().until(date, DAYS))
      .orElse(null);
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
    return ((ServerConnector)server.getConnectors()[0]).getLocalPort();
  }

  public static OllieServerBuilder builder() {
    return new OllieServerBuilder();
  }
}
