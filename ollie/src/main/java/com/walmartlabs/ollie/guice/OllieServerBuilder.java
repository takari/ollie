package com.walmartlabs.ollie.guice;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.apache.shiro.realm.Realm;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.walmartlabs.ollie.OllieServer;
import com.walmartlabs.ollie.model.FilterDefinition;
import com.walmartlabs.ollie.model.OllieServerDefinitionBuilder;

public class OllieServerBuilder {

  private String name = "application";
  private String api = "/api";
  private String docs = "/docs";
  private String title = "Swagger Console";
  private String description = "Swagger Console";
  private String packageToScan;  
  private List<Module> modules = Lists.newArrayList();
  // Security
  private List<Class<? extends Realm>> realms;
  private List<FilterDefinition> filterChains;
  
  protected OllieServerDefinitionBuilder webServerDefinitionBuilder;
  
  @Deprecated
  /** @deprecated Use {@link OllieServer.builder()} */
  public OllieServerBuilder() {
    webServerDefinitionBuilder = new OllieServerDefinitionBuilder();
  }
  
  public OllieServer build() {           
    contextListener(new OllieServletContextListener(this));
    filter("/*").through(CrossOriginFilter.class);
    filter("/*").through(GuiceFilter.class);
    if (docs != null) {
      at(docs).resource("swagger-ui", ImmutableList.of("index.html"));
    }
    return new OllieServer(webServerDefinitionBuilder.build());
  }

  public OllieServerBuilder title(String title) {
    this.title = title;
    return this;
  }
   
  public String title() {
    return title;
  }
  
  public OllieServerBuilder description(String description) {
    this.description = description;
    return this;
  }
   
  public String description() {
    return description;
  }
  
  public OllieServerBuilder name(String name) {
    this.name = name;
    return this;
  }
   
  public String name() {
    return name;
  }
  
  public OllieServerBuilder api(String api) {
    this.api = api;
    return this;
  }

  public String api() {
    return api;
  }
  
  public OllieServerBuilder docs(String docs) {
    this.docs = docs;
    return this;
  }

  public String docs() {
    return docs;
  }
  
  public OllieServerBuilder packageToScan(String packageToScan) {
    this.packageToScan = packageToScan;
    return this;
  }
  
  public String packageToScan() {
    return packageToScan;
  }
  
  public OllieServerBuilder module(Module module) {
    this.modules.add(module);
    return this;
  }
  
  public List<Module> modules() {
    return modules;
  }
  
  public OllieServerBuilder realm(Class<? extends Realm> realm) {
    if(realms == null) {
      realms = Lists.newArrayList();
    }
    realms.add(realm);
    return this;
  }  
  
  public List<Class<? extends Realm>> realms() {
    return realms;
  }
  
  public OllieServerBuilder filterChain(String pattern, Class<? extends Filter> filterClass) {
    if(filterChains == null) {
      filterChains = Lists.newArrayList();
    }
    filterChains.add(new FilterDefinition(new String[] {pattern}, filterClass));
    return this;
  }
  
  public List<FilterDefinition> filterChains() {
    return filterChains;
  }
  
  // WebServerBuilder
  
  public OllieServerBuilder contextListener(ServletContextListener contextListener) {
    webServerDefinitionBuilder.contextListener(contextListener);
    return this;
  }

  public OllieServerBuilder port(int port) {
    webServerDefinitionBuilder.port(port);
    return this;
  }

  public OllieServerBuilder sessionsEnabled(boolean sessionsEnabled) {
    webServerDefinitionBuilder.sessionsEnabled(sessionsEnabled);
    return this;
  }

  public OllieServerBuilder filter(String... patterns) {
    webServerDefinitionBuilder.filter(patterns);
    return this;
  }

  public OllieServerBuilder through(Class<? extends Filter> filterClass) {
    webServerDefinitionBuilder.through(filterClass);
    return this;
  }

  public OllieServerBuilder serve(String pattern) {
    return serve(pattern, Lists.<String>newArrayList());
  }

  public OllieServerBuilder serve(String pattern, List<String> welcomeFiles) {
    webServerDefinitionBuilder.serve(pattern, welcomeFiles);
    return this;
  }

  public OllieServerBuilder with(Class<? extends HttpServlet> servletClass) {
    return with(servletClass, Maps.<String,String>newHashMap());
  }

  public OllieServerBuilder with(Class<? extends HttpServlet> servletClass, Map<String, String> parameters) {
    webServerDefinitionBuilder.with(servletClass, parameters);
    return this;
  }

  public OllieServerBuilder with(HttpServlet servlet) {
    webServerDefinitionBuilder.with(servlet);
    return this;
  }

  public OllieServerBuilder with(File webapp) {
    webServerDefinitionBuilder.with(webapp);
    return this;
  }  

  public OllieServerBuilder with(File webapp, SecurityHandler securityHandler) {
    webServerDefinitionBuilder.with(webapp, securityHandler);
    return this;
  }  
  
  public OllieServerBuilder at(String path) {
    webServerDefinitionBuilder.at(path);
    return this;
  }

  public OllieServerBuilder resource(String resource) {
    return resource(resource, Lists.<String>newArrayList(), false);
  }

  public OllieServerBuilder resourceWithListing(String resource) {
    return resource(resource, Lists.<String>newArrayList(), true);
  }
  
  
  public OllieServerBuilder resource(String resource, List<String> welcomeFiles) {
    return resource(resource, welcomeFiles, false);
  }

  public OllieServerBuilder resource(String resource, List<String> welcomeFiles, boolean listing) {
    webServerDefinitionBuilder.resource(resource, welcomeFiles, listing);
    return this;
  }
  
  public OllieServerBuilder sites(File sitesDirectory) {
    webServerDefinitionBuilder.sites(sitesDirectory);
    return this;
  }
}
