package io.takari.server.model;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.security.SecurityHandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class WebServerDefinitionBuilder {

  private WebServerDefinition webServerDefinition;
  private StaticResourceDefinition staticContentDefinition;
  private FilterDefinition filterDefinition;
  private ServletDefinition servletDefinition;

  public WebServerDefinitionBuilder() {
    webServerDefinition = new WebServerDefinition();
  }

  public WebServerDefinitionBuilder port(int port) {
    webServerDefinition.setPort(port);
    return this;
  }

  public WebServerDefinitionBuilder filter(String pattern) {
    filterDefinition = new FilterDefinition();
    filterDefinition.setPattern(pattern);
    return this;
  }

  public WebServerDefinitionBuilder through(Class<? extends Filter> filterClass) {
    filterDefinition.setFilterClass(filterClass);
    webServerDefinition.addFilterDefinition(filterDefinition);
    return this;
  }

  public WebServerDefinitionBuilder serve(String pattern) {
    return serve(pattern, Lists.<String>newArrayList());
  }

  public WebServerDefinitionBuilder serve(String pattern, List<String> welcomeFiles) {
    servletDefinition = new ServletDefinition();
    servletDefinition.setPattern(pattern);
    return this;
  }

  public WebServerDefinitionBuilder with(Class<? extends HttpServlet> servletClass) {
    return with(servletClass, Maps.<String,String>newHashMap());
  }

  public WebServerDefinitionBuilder with(Class<? extends HttpServlet> servletClass, Map<String, String> parameters) {
    servletDefinition.setServletClass(servletClass);
    servletDefinition.setParameters(parameters);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;
  }

  public WebServerDefinitionBuilder with(HttpServlet servlet) {
    servletDefinition.setServlet(servlet);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;
  }

  public WebServerDefinitionBuilder with(File webapp) {
    servletDefinition.setWar(webapp);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;
  }  
  
  public WebServerDefinitionBuilder with(File webapp, SecurityHandler securityHandler) {
    servletDefinition.setWar(webapp);
    servletDefinition.setSecurityHandler(securityHandler);
    webServerDefinition.addServletDefinition(servletDefinition);
    return this;    
  }  
  
  public WebServerDefinitionBuilder at(String path) {
    staticContentDefinition = new StaticResourceDefinition();
    staticContentDefinition.setPath(path);
    return this;
  }

  public WebServerDefinitionBuilder resource(String resource) {
    return resource(resource, Lists.<String>newArrayList());
  }

  public WebServerDefinitionBuilder resource(String resource, List<String> welcomeFiles) {
    return resource(resource, welcomeFiles, false);
  } 

  public WebServerDefinitionBuilder resource(String resource, List<String> welcomeFiles, boolean listing) {
    staticContentDefinition.setResource(resource);
    staticContentDefinition.setListing(listing);
    if (welcomeFiles != null) {
      staticContentDefinition.setWelcomeFiles(welcomeFiles);
    }
    webServerDefinition.addStaticContentDefinition(staticContentDefinition);
    return this;
  }
  
  public WebServerDefinitionBuilder sites(File sitesDirectory) {
    SitesDefinition sitesDefinition = new SitesDefinition();
    sitesDefinition.setSitesDirectory(sitesDirectory);
    webServerDefinition.setSitesDefinition(sitesDefinition);    
    return this;
  }  
  
  public WebServerDefinition build() {
    return webServerDefinition;
  }

  public WebServerDefinitionBuilder contextListener(ServletContextListener contextListener) {
    webServerDefinition.contextListener(contextListener);
    return this;
    
  }
}
