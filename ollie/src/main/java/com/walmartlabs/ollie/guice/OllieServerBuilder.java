package com.walmartlabs.ollie.guice;

import java.util.List;

import javax.inject.Provider;
import javax.servlet.Filter;

import org.apache.shiro.realm.Realm;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.walmartlabs.ollie.WebServer;
import com.walmartlabs.ollie.WebServerBuilder;
import com.walmartlabs.ollie.model.FilterDefinition;

public class OllieServerBuilder extends WebServerBuilder {

  private String name = "application";
  private String api = "/api";
  private String docs = "/docs";
  private String title = "Swagger Console";
  private String description = "Swagger Console";
  private String packageToScan;  
  // We need to use Google's Provider because Shiro doesn't use JSR330
  private Provider<Realm> realm;
  private List<Module> modules = Lists.newArrayList();
  // Security
  private List<Class<? extends Realm>> realms;
  private List<FilterDefinition> filterChains;
  
  public WebServer build() {    
    
    OllieServerConfiguration config = OllieServerConfiguration.builder()
      .name(name)
      .api(api)
      .docs(docs)
      .title(title)
      .description(description)
      .packageToScan(packageToScan)
      .modules(modules)
      .realm(realm)
      .realms(realms)
      .filterChains(filterChains)
      .build();
    
    contextListener(new OllieServletContextListener(config));
    filter("/*").through(CrossOriginFilter.class);
    filter("/*").through(GuiceFilter.class);
    if (docs != null) {
      at(docs).resource("swagger-ui", ImmutableList.of("index.html"));
    }
    return new WebServer(webServerDefinitionBuilder.build());
  }

  public OllieServerBuilder name(String name) {
    this.name = name;
    return this;
  }
    
  public OllieServerBuilder port(int port) {
    super.port(port);
    return this;
  }

  public OllieServerBuilder sessionsEnabled(boolean sessionsEnabled) {
    super.sessionsEnabled(sessionsEnabled);
    return this;
  }

  public OllieServerBuilder api(String api) {
    this.api = api;
    return this;
  }

  public OllieServerBuilder docs(String docs) {
    this.docs = docs;
    return this;
  }

  public OllieServerBuilder packageToScan(String packageToScan) {
    this.packageToScan = packageToScan;
    return this;
  }
  
  public OllieServerBuilder module(Module module) {
    this.modules.add(module);
    return this;
  }
  
  public OllieServerBuilder realm(Provider<Realm> realm) {
    this.realm = realm;
    return this;
  }

  public OllieServerBuilder realm(Class<? extends Realm> realm) {
    if(realms == null) {
      realms = Lists.newArrayList();
    }
    realms.add(realm);
    return this;
  }  
  
  public OllieServerBuilder filterChain(String pattern, Class<? extends Filter> filterClass) {
    if(filterChains == null) {
      filterChains = Lists.newArrayList();
    }
    filterChains.add(new FilterDefinition(pattern, filterClass));
    return this;
  }
}
