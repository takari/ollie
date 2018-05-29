package com.walmartlabs.ollie;

import java.io.File;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.common.base.Preconditions;
import com.walmartlabs.ollie.model.FilterDefinition;
import com.walmartlabs.ollie.model.ServletDefinition;
import com.walmartlabs.ollie.model.StaticResourceDefinition;
import com.walmartlabs.ollie.model.WebServerDefinition;

// http://stackoverflow.com/questions/20043097/jetty-9-embedded-adding-handlers-during-runtime

public class WebServer  {

  protected final Server server;

  public WebServer(WebServerDefinition webServerDefinition) {
    this.server = build(webServerDefinition);
  }

  public void start() {
    try {
      server.start();
    } catch (
    Exception e) {
      throw new RuntimeException(e);
    }    
  }

  public void startAndWait() {
    try {
      server.start();
      server.join();
    } catch (
    Exception e) {
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

  public Server build(WebServerDefinition webServerDefinition) {
    QueuedThreadPool threadPool = new QueuedThreadPool(webServerDefinition.getMaxThreads());
    threadPool.setMinThreads(webServerDefinition.getMinThreads());
    // threadPool.setIdleTimeout(Ints.checkedCast(config.getThreadMaxIdleTime().toMillis()));
    threadPool.setName("http-worker");

    Server server = new Server(threadPool);

    HttpConfiguration httpConfiguration = new HttpConfiguration();
    httpConfiguration.setSendServerVersion(false);
    httpConfiguration.setSendXPoweredBy(false);

    ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));
    httpConnector.setName("http");
    httpConnector.setPort(webServerDefinition.getPort());
    // httpConnector.setIdleTimeout(config.getNetworkMaxIdleTime().toMillis());
    // httpConnector.setHost(nodeInfo.getBindIp().getHostAddress());
    server.addConnector(httpConnector);
    ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

    for (StaticResourceDefinition definition : webServerDefinition.getStaticContentDefinitions()) {
      String[] welcomeFiles = definition.getWelcomeFiles().toArray(new String[definition.getWelcomeFiles().size()]);
      File fileResourcePath = new File(definition.getResource());
      if (fileResourcePath.exists()) {
        contextHandlerCollection.addHandler(fileResourceHandler(definition));
      } else {
        contextHandlerCollection.addHandler(classpathResourceHandler(definition.getPath(), definition.getResource(), welcomeFiles));
      }
    }

    int options = ServletContextHandler.NO_SESSIONS;
    if (webServerDefinition.isSessionsEnabled()) {
      options |= ServletContextHandler.SESSIONS;
    }
    ServletContextHandler applicationContext = new ServletContextHandler(options);
    construct(webServerDefinition, applicationContext);
    contextHandlerCollection.addHandler(applicationContext);

    if (webServerDefinition.getSitesDefinition() != null) {
      DeploymentManager deploymentManager = new DeploymentManager();
      deploymentManager.setContexts(contextHandlerCollection);
      StaticSitesProvider staticSitesAppProvider = new StaticSitesProvider();
      staticSitesAppProvider.setMonitoredDirResource(new PathResource(webServerDefinition.getSitesDefinition().getSitesDirectory().toPath()));
      deploymentManager.addAppProvider(staticSitesAppProvider);
      server.addBean(deploymentManager);
    }

    StatisticsHandler statisticsHandler = new StatisticsHandler();
    statisticsHandler.setHandler(contextHandlerCollection);
    server.setHandler(statisticsHandler);

    return server;
  }

  protected void construct(WebServerDefinition webServerDefinition, ServletContextHandler applicationContext) {

    if (webServerDefinition.contextListener() != null) {
      applicationContext.addEventListener(webServerDefinition.contextListener());
    }

    for (FilterDefinition filterDefinition : webServerDefinition.getFilterDefintions()) {
      String[] patterns = filterDefinition.getPatterns();
      for (String pattern : patterns) {
        applicationContext.addFilter(new FilterHolder(filterDefinition.getFilterClass()), pattern, null);
      }
    }
    for (ServletDefinition servletDefinition : webServerDefinition.getServletDefinitions()) {
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
}
