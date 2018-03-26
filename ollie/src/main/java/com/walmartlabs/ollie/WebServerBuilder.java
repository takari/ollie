package com.walmartlabs.ollie;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.security.SecurityHandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walmartlabs.ollie.guice.JaxRsServerBuilder;
import com.walmartlabs.ollie.model.WebServerDefinitionBuilder;

public class WebServerBuilder {

  protected WebServerDefinitionBuilder webServerDefinitionBuilder;
  
  public WebServerBuilder() {
    webServerDefinitionBuilder = new WebServerDefinitionBuilder();
  }

  public WebServerBuilder contextListener(ServletContextListener contextListener) {
    webServerDefinitionBuilder.contextListener(contextListener);
    return this;
  }

  public WebServerBuilder port(int port) {
    webServerDefinitionBuilder.port(port);
    return this;
  }

  public WebServerBuilder filter(String pattern) {
    webServerDefinitionBuilder.filter(pattern);
    return this;
  }

  public WebServerBuilder through(Class<? extends Filter> filterClass) {
    webServerDefinitionBuilder.through(filterClass);
    return this;
  }

  public WebServerBuilder serve(String pattern) {
    return serve(pattern, Lists.<String>newArrayList());
  }

  public WebServerBuilder serve(String pattern, List<String> welcomeFiles) {
    webServerDefinitionBuilder.serve(pattern, welcomeFiles);
    return this;
  }

  public WebServerBuilder with(Class<? extends HttpServlet> servletClass) {
    return with(servletClass, Maps.<String,String>newHashMap());
  }

  public WebServerBuilder with(Class<? extends HttpServlet> servletClass, Map<String, String> parameters) {
    webServerDefinitionBuilder.with(servletClass, parameters);
    return this;
  }

  public WebServerBuilder with(HttpServlet servlet) {
    webServerDefinitionBuilder.with(servlet);
    return this;
  }

  public WebServerBuilder with(File webapp) {
    webServerDefinitionBuilder.with(webapp);
    return this;
  }  

  public WebServerBuilder with(File webapp, SecurityHandler securityHandler) {
    webServerDefinitionBuilder.with(webapp, securityHandler);
    return this;
  }  
  
  public WebServerBuilder at(String path) {
    webServerDefinitionBuilder.at(path);
    return this;
  }

  public WebServerBuilder resource(String resource) {
    return resource(resource, Lists.<String>newArrayList(), false);
  }

  public WebServerBuilder resourceWithListing(String resource) {
    return resource(resource, Lists.<String>newArrayList(), true);
  }
  
  
  public WebServerBuilder resource(String resource, List<String> welcomeFiles) {
    return resource(resource, welcomeFiles, false);
  }

  public WebServerBuilder resource(String resource, List<String> welcomeFiles, boolean listing) {
    webServerDefinitionBuilder.resource(resource, welcomeFiles, listing);
    return this;
  }
  
  
  public WebServer build() {
    return new WebServer(webServerDefinitionBuilder.build());
  }

  public WebServerBuilder sites(File sitesDirectory) {
    webServerDefinitionBuilder.sites(sitesDirectory);
    return this;
  }
}
