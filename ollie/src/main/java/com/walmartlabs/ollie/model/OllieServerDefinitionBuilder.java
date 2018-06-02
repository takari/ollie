package com.walmartlabs.ollie.model;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.security.SecurityHandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class OllieServerDefinitionBuilder {

  private OllieServerDefinition webServerDefinition;
  private StaticResourceDefinition staticContentDefinition;
  private FilterDefinition filterDefinition;
  private ServletDefinition servletDefinition;

  public OllieServerDefinitionBuilder() {
    webServerDefinition = new OllieServerDefinition();
  }

  public OllieServerDefinitionBuilder port(int port) {
    webServerDefinition.setPort(port);
    return this;
  }


  public OllieServerDefinitionBuilder filter(String... patterns) {
    filterDefinition = new FilterDefinition();
    filterDefinition.setPatterns(patterns);
    return this;
  }

  public OllieServerDefinitionBuilder through(Class<? extends Filter> filterClass) {
    filterDefinition.setFilterClass(filterClass);
    webServerDefinition.addFilterDefinition(filterDefinition);
    return this;
  }

  public OllieServerDefinitionBuilder serve(String pattern) {
    return serve(pattern, Lists.<String>newArrayList());
  }

  public OllieServerDefinitionBuilder serve(String pattern, List<String> welcomeFiles) {
    servletDefinition = new ServletDefinition();
    servletDefinition.setPattern(pattern);
    return this;
  }

  public OllieServerDefinitionBuilder with(Class<? extends HttpServlet> servletClass) {
    return with(servletClass, Maps.<String,String>newHashMap());
  }

  public OllieServerDefinitionBuilder with(Class<? extends HttpServlet> servletClass, Map<String, String> parameters) {
    servletDefinition.setServletClass(servletClass);
    servletDefinition.setParameters(parameters);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;
  }

  public OllieServerDefinitionBuilder with(HttpServlet servlet) {
    servletDefinition.setServlet(servlet);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;
  }

  public OllieServerDefinitionBuilder with(File webapp) {
    servletDefinition.setWar(webapp);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;
  }  
  
  public OllieServerDefinitionBuilder with(File webapp, SecurityHandler securityHandler) {
    servletDefinition.setWar(webapp);
    servletDefinition.setSecurityHandler(securityHandler);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;    
  }  
  
  public OllieServerDefinitionBuilder at(String path) {
    staticContentDefinition = new StaticResourceDefinition();
    staticContentDefinition.setPath(path);
    return this;
  }

  public OllieServerDefinitionBuilder resource(String resource) {
    return resource(resource, Lists.<String>newArrayList());
  }

  public OllieServerDefinitionBuilder resource(String resource, List<String> welcomeFiles) {
    return resource(resource, welcomeFiles, false);
  } 

  public OllieServerDefinitionBuilder resource(String resource, List<String> welcomeFiles, boolean listing) {
    staticContentDefinition.setResource(resource);
    staticContentDefinition.setListing(listing);
    if (welcomeFiles != null) {
      staticContentDefinition.setWelcomeFiles(welcomeFiles);
    }
    webServerDefinition.addStaticContentDefinition(staticContentDefinition);
    return this;
  }
  
  public OllieServerDefinitionBuilder sites(File sitesDirectory) {
    SitesDefinition sitesDefinition = new SitesDefinition();
    sitesDefinition.setSitesDirectory(sitesDirectory);
    webServerDefinition.setSitesDefinition(sitesDefinition);    
    return this;
  }

  public OllieServerDefinitionBuilder sessionsEnabled(boolean sessionEnabled) {
    webServerDefinition.setSessionsEnabled(sessionEnabled);
    return this;
  }
  
  public OllieServerDefinition build() {
    return webServerDefinition;
  }

  public OllieServerDefinitionBuilder contextListener(ServletContextListener contextListener) {
    webServerDefinition.contextListener(contextListener);
    return this;
    
  }
}
