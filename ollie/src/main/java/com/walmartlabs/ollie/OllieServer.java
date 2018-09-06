package com.walmartlabs.ollie;

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

// http://stackoverflow.com/questions/20043097/jetty-9-embedded-adding-handlers-during-runtime

public class OllieServer {

  private static Logger logger = LoggerFactory.getLogger(OllieServer.class);
  protected final Server server;
  private final Optional<ZonedDateTime> certificateExpiration;

  public OllieServer(OllieServerBuilder webServerDefinition) {
    this.server = build(webServerDefinition);
    this.certificateExpiration = loadAllX509Certificates(webServerDefinition).stream()
      .map(X509Certificate::getNotAfter)
      .min(naturalOrder())
      .map(date -> ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
  }

  public Server build(OllieServerBuilder config) {
    logger.info("Constructing Jetty server...");
    logger.info("maxThreads = {}", config.maxThreads);
    logger.info("minThreads = {}", config.minThreads);
    logger.info("port = {}", config.port);
    QueuedThreadPool threadPool = new QueuedThreadPool(config.maxThreads);
    threadPool.setMinThreads(config.minThreads);
    // threadPool.setIdleTimeout(Ints.checkedCast(config.getThreadMaxIdleTime().toMillis()));
    threadPool.setName("http-worker");
    Server server = new Server(threadPool);

    if (config.jmxEnabled) {
      MBeanContainer mbeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
      server.addBean(mbeanContainer);
    }

    HttpConfiguration baseHttpConfiguration = new HttpConfiguration();
    baseHttpConfiguration.setSendServerVersion(false);
    baseHttpConfiguration.setSendXPoweredBy(false);

    ServerConnector httpsConnector;
    if (config.sslEnabled) {
      logger.info("HTTPS connector enabled");
      HttpConfiguration httpsConfiguration = new HttpConfiguration(baseHttpConfiguration);
      httpsConfiguration.setSecureScheme("https");
      httpsConfiguration.setSecurePort(config.port());
      httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

      SslContextFactory sslContextFactory = new SslContextFactory();
      Optional<KeyStore> pemKeyStore = tryLoadPemKeyStore(config);
      if (pemKeyStore.isPresent()) {
        sslContextFactory.setKeyStore(pemKeyStore.get());
        sslContextFactory.setKeyStorePassword("");
      } else {
        throw new RuntimeException("SSL has been configured but the keystore file cannot be loaded: " + config.keystorePath);
      }
      if (config.truststorePath != null) {
        Optional<KeyStore> pemTrustStore = tryLoadPemTrustStore(config);
        if (pemTrustStore.isPresent()) {
          sslContextFactory.setTrustStore(pemTrustStore.get());
          sslContextFactory.setTrustStorePassword("");
        } else {
          throw new RuntimeException("SSL has been configured but the truststore file cannot be loaded: " + config.keystorePath);
        }
      }

      List<String> includedCipherSuites = config.includedCipherSuites;
      sslContextFactory.setIncludeCipherSuites(includedCipherSuites.toArray(new String[includedCipherSuites.size()]));
      List<String> excludedCipherSuites = config.excludedCipherSuites;
      sslContextFactory.setExcludeCipherSuites(excludedCipherSuites.toArray(new String[excludedCipherSuites.size()]));
      sslContextFactory.setSecureRandomAlgorithm(config.secureRandomAlgorithm);
      sslContextFactory.setWantClientAuth(true);
      sslContextFactory.setSslSessionTimeout((int) config.sslSessionTimeout);
      sslContextFactory.setSslSessionCacheSize(config.sslSessionCacheSize);
      SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");

      httpsConnector = new ServerConnector(server, sslConnectionFactory, new HttpConnectionFactory(httpsConfiguration));
      httpsConnector.setName("https");
      httpsConnector.setPort(config.port);
      //httpsConnector.setIdleTimeout(config.getNetworkMaxIdleTime().toMillis());
      //httpsConnector.setHost(nodeInfo.getBindIp().getHostAddress());
      //httpsConnector.setAcceptQueueSize(config.getHttpAcceptQueueSize());

      server.addConnector(httpsConnector);
    }

    ServerConnector httpConnector;
    if (!config.sslEnabled()) {
      logger.info("HTTP connector enabled.");
      httpConnector = new ServerConnector(server, new HttpConnectionFactory(baseHttpConfiguration));
      httpConnector.setName("http");
      httpConnector.setPort(config.port);
      // httpConnector.setIdleTimeout(config.getNetworkMaxIdleTime().toMillis());
      // httpConnector.setHost(nodeInfo.getBindIp().getHostAddress());
      server.addConnector(httpConnector);
    }

    ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

    if (config.staticContentDefinitions.size() > 0) {
      logger.info("Setting up static resources handlers.");
      for (StaticResourceDefinition definition : config.staticContentDefinitions) {
        String[] welcomeFiles = definition.getWelcomeFiles().toArray(new String[definition.getWelcomeFiles().size()]);
        File fileResourcePath = new File(definition.getResource());
        if (fileResourcePath.exists()) {
          contextHandlerCollection.addHandler(fileResourceHandler(definition));
        } else {
          contextHandlerCollection.addHandler(classpathResourceHandler(definition.getPath(), definition.getResource(), welcomeFiles));
        }
      }
    }

    int options = ServletContextHandler.NO_SESSIONS;
    if (config.sessionsEnabled) {
      options |= ServletContextHandler.SESSIONS;
    }

    ServletContextHandler applicationContext = new ServletContextHandler(options);

    if (config.contextListener != null) {
      logger.info("Adding context listener {}.", config.contextListener.getClass().getName());
      applicationContext.addEventListener(config.contextListener);
    }

    if (config.filterDefintions.size() > 0) {
      logger.info("Setting up servlet filters.");      
      for (FilterDefinition filterDefinition : config.filterDefintions) {
        String[] patterns = filterDefinition.getPatterns();
        for (String pattern : patterns) {
          logger.info("Filter {} -> {}.", pattern, filterDefinition.getFilterClass());
          applicationContext.addFilter(new FilterHolder(filterDefinition.getFilterClass()), pattern, null);
        }
      }
    }

    if (config.servletDefinitions.size() > 0) {
      logger.info("Setting up servlets.");
      for (ServletDefinition servletDefinition : config.servletDefinitions) {
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
    String[] welcomeFiles = definition.getWelcomeFiles().toArray(new String[definition.getWelcomeFiles().size()]);
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
