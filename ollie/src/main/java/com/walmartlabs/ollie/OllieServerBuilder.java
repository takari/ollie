package com.walmartlabs.ollie;

import java.io.File;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import com.walmartlabs.ollie.guice.OllieServletContextListener;
import com.walmartlabs.ollie.model.FilterDefinition;
import com.walmartlabs.ollie.model.ServletDefinition;
import com.walmartlabs.ollie.model.StaticResourceDefinition;
import com.walmartlabs.ollie.util.ConnectionSpec;

public class OllieServerBuilder {

  String name = "application";
  String api = "/api";
  String docs = "/docs";
  String title = "Swagger Console";
  String description = "Swagger Console";
  String packageToScan;  
  List<Module> modules = Lists.newArrayList();
  // Security
  List<Class<? extends Realm>> realms;
  List<FilterDefinition> filterChains;
  String[] apiPatterns;    
  //  
  StaticResourceDefinition staticContentDefinition;
  FilterDefinition filterDefinition;
  ServletDefinition servletDefinition;  
  //
  int port = 8080;
  int minThreads = 2;
  int maxThreads = 200;
  int threadMaxIdleTime;
  boolean sessionsEnabled = false;
  boolean sslEnabled = false;
  boolean secureCookiesEnabled = false;
  List<FilterDefinition> filterDefintions = Lists.newArrayList();
  List<ServletDefinition> servletDefinitions = Lists.newArrayList();
  List<StaticResourceDefinition> staticContentDefinitions = Lists.newArrayList();
  ServletContextListener contextListener;  
  //
  String keystorePath;
  String keystorePassword;
  String truststorePath;
  // https://stackoverflow.com/questions/27622625/securerandom-with-nativeprng-vs-sha1prng
  String secureRandomAlgorithm = new SecureRandom().getAlgorithm();                                                                                                                           
  List<String> includedCipherSuites = ConnectionSpec.MODERN_TLS.cipherSuites();                                                                                                 
  List<String> excludedCipherSuites = ImmutableList.of();                                                                                                                                                                                                                                                                   
  double sslSessionTimeout = TimeUnit.SECONDS.convert(4, TimeUnit.HOURS);                                                                                                   
  int sslSessionCacheSize = 10_000;                       
  // Secrets
  File secrets;

  public OllieServer build() {           
    this.contextListener  = new OllieServletContextListener(this);
    filter("/*").through(CrossOriginFilter.class);
    filter("/*").through(GuiceFilter.class);
    if (docs != null) {
      at(docs).resource("swagger-ui", ImmutableList.of("index.html"));
    }    
    return new OllieServer(this);
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

  public OllieServerBuilder apiPatterns(String... apiPatterns) {
    this.apiPatterns = apiPatterns;
    return this;
  }

  public String[] apiPatterns() {
    return apiPatterns;
  }
    
  public OllieServerBuilder port(int port) {
    this.port = port;
    return this;
  }

  public int port() {
    return port;
  }
  
  public int minThreads() {
    return minThreads;
  }
  
  public int maxThreads() {
    return maxThreads;
  }
  
  public OllieServerBuilder sessionsEnabled(boolean sessionsEnabled) {
    this.sessionsEnabled = sessionsEnabled;
    return this;
  }

  public boolean sslEnabled() {
    return sslEnabled;
  }
  
  public OllieServerBuilder sslEnabled(boolean sslEnabled) {
    this.sslEnabled = sslEnabled;
    return this;
  }

  public boolean secureCookiesEnabled() {
    return secureCookiesEnabled;
  }

  public OllieServerBuilder secureCookiesEnabled(boolean secureCookiesEnabled) {
    this.secureCookiesEnabled = secureCookiesEnabled;
    return this;
  }

  public OllieServerBuilder keystorePath(String keystorePath) {
    this.keystorePath = keystorePath;
    return this;
  }
  
  public OllieServerBuilder keystorePassword(String keystorePassword) {
    this.keystorePassword = keystorePassword;
    return this;
  }

  public OllieServerBuilder truststorePath(String truststorePath) {
    this.truststorePath = truststorePath;
    return this;
  }  
    
  public OllieServerBuilder filter(String... patterns) {
    filterDefinition = new FilterDefinition();
    filterDefinition.setPatterns(patterns);
    return this;
  }

  public OllieServerBuilder through(Class<? extends Filter> filterClass) {
    filterDefinition.setFilterClass(filterClass);
    filterDefintions.add(filterDefinition);
    return this;
  }

  public OllieServerBuilder serve(String pattern) {
    return serve(pattern, Lists.<String>newArrayList());
  }

  public OllieServerBuilder serve(String pattern, List<String> welcomeFiles) {
    servletDefinition = new ServletDefinition();
    servletDefinition.setPattern(pattern);
    return this;
  }

  public OllieServerBuilder with(Class<? extends HttpServlet> servletClass) {
    return with(servletClass, Maps.<String,String>newHashMap());
  }

  public OllieServerBuilder with(Class<? extends HttpServlet> servletClass, Map<String, String> parameters) {
    servletDefinition.setServletClass(servletClass);
    servletDefinition.setParameters(parameters);
    servletDefinitions.add(servletDefinition);
    return this;
  }

  public OllieServerBuilder with(HttpServlet servlet) {
    servletDefinition.setServlet(servlet);
    servletDefinitions.add(servletDefinition);
    return this;
  }

  public OllieServerBuilder with(File webapp) {
    servletDefinition.setWar(webapp);
    servletDefinitions.add(servletDefinition);
    return this;
  }  
  
  public OllieServerBuilder with(File webapp, SecurityHandler securityHandler) {
    servletDefinition.setWar(webapp);
    servletDefinition.setSecurityHandler(securityHandler);
    servletDefinitions.add(servletDefinition);
    return this;    
  }  
  
  public OllieServerBuilder at(String path) {
    staticContentDefinition = new StaticResourceDefinition();
    staticContentDefinition.setPath(path);
    return this;
  }

  public OllieServerBuilder resource(String resource) {
    return resource(resource, Lists.<String>newArrayList());
  }

  public OllieServerBuilder resource(String resource, List<String> welcomeFiles) {
    return resource(resource, welcomeFiles, false);
  } 

  public OllieServerBuilder resource(String resource, List<String> welcomeFiles, boolean listing) {
    staticContentDefinition.setResource(resource);
    staticContentDefinition.setListing(listing);
    if (welcomeFiles != null) {
      staticContentDefinition.setWelcomeFiles(welcomeFiles);
    }
    staticContentDefinitions.add(staticContentDefinition);
    return this;
  }

  public OllieServerBuilder secrets(File secrets) {
    this.secrets = secrets;
    return this;
  }

  public File secrets() {
    return secrets;
  }
}
