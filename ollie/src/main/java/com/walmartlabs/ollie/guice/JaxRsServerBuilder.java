package com.walmartlabs.ollie.guice;

import java.util.List;

import javax.inject.Provider;

import org.apache.shiro.realm.Realm;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.walmartlabs.ollie.WebServer;
import com.walmartlabs.ollie.WebServerBuilder;

public class JaxRsServerBuilder extends WebServerBuilder {

  private String name = "application";
  private String api = "/api";
  private String docs = "/docs";
  private String title = "Swagger Console";
  private String description = "Swagger Console";
  private String packageToScan;  
  // We need to use Google's Provider because Shiro doesn't use JSR330
  private Provider<Realm> realm;
  private List<Module> modules = Lists.newArrayList();
  
  public WebServer build() {    
    
    JaxRsServerConfiguration config = JaxRsServerConfiguration.builder()
      .name(name)
      .api(api)
      .docs(docs)
      .title(title)
      .description(description)
      .packageToScan(packageToScan)
      .modules(modules)
      .realm(realm)
      .build();
    
    contextListener(new AppServletContextListener(config));
    filter("/*").through(CrossOriginFilter.class);
    filter("/*").through(GuiceFilter.class);
    if (docs != null) {
      at(docs).resource("swagger-ui", ImmutableList.of("index.html"));
    }
    return new WebServer(webServerDefinitionBuilder.build());
  }

  public JaxRsServerBuilder name(String name) {
    this.name = name;
    return this;
  }
  
  
  public JaxRsServerBuilder port(int port) {
    super.port(port);
    return this;
  }

  public JaxRsServerBuilder api(String api) {
    this.api = api;
    return this;
  }

  public JaxRsServerBuilder docs(String docs) {
    this.docs = docs;
    return this;
  }

  public JaxRsServerBuilder packageToScan(String packageToScan) {
    this.packageToScan = packageToScan;
    return this;
  }
  
  public JaxRsServerBuilder module(Module module) {
    this.modules.add(module);
    return this;
  }
  
  public JaxRsServerBuilder realm(Provider<Realm> realm) {
    this.realm = realm;
    return this;
  }
}
