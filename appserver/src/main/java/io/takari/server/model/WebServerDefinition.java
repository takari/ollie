package io.takari.server.model;

import java.util.List;

import javax.servlet.ServletContextListener;

import com.google.common.collect.Lists;

public class WebServerDefinition {
  int port = 8080;
  int minThreads = 2;
  int maxThreads = 200;
  int threadMaxIdleTime;
  List<FilterDefinition> filterDefintions = Lists.newArrayList();
  List<ServletDefinition> servletDefinitions = Lists.newArrayList();
  List<StaticResourceDefinition> staticContentDefinitions = Lists.newArrayList();
  SitesDefinition sitesDefinition;
  ServletContextListener contextListener;

  public void addFilterDefinition(FilterDefinition filterDefinition) {
    filterDefintions.add(filterDefinition);
  }

  public void addServletDefinition(ServletDefinition servletDefinition) {
    servletDefinitions.add(servletDefinition);
  }

  public void addStaticContentDefinition(StaticResourceDefinition staticContentDefinition) {
    staticContentDefinitions.add(staticContentDefinition);
  }

  public void setPort(int port) {
    this.port = port;

  }

  public int getPort() {
    return port;
  }

  public int getMinThreads() {
    return minThreads;
  }

  public int getMaxThreads() {
    return maxThreads;
  }

  public int getThreadMaxIdleTime() {
    return threadMaxIdleTime;
  }

  public List<FilterDefinition> getFilterDefintions() {
    return filterDefintions;
  }

  public List<ServletDefinition> getServletDefinitions() {
    return servletDefinitions;
  }

  public List<StaticResourceDefinition> getStaticContentDefinitions() {
    return staticContentDefinitions;
  }

  public SitesDefinition getSitesDefinition() {
    return sitesDefinition;
  }

  public void setSitesDefinition(SitesDefinition sitesDefinition) {
    this.sitesDefinition = sitesDefinition;
  }

  @Override
  public String toString() {
    return "WebServerDefinition [port=" + port + ", minThreads=" + minThreads + ", maxThreads=" + maxThreads + ", threadMaxIdleTime=" + threadMaxIdleTime + ", filterDefintions=" + filterDefintions
      + ", servletDefinitions=" + servletDefinitions + ", staticContentDefinitions=" + staticContentDefinitions + ", sitesDefinition=" + sitesDefinition + "]";
  }

  public void contextListener(ServletContextListener contextListener) {
    this.contextListener = contextListener;
  }

  public ServletContextListener contextListener() {
    return contextListener;
  }
}
