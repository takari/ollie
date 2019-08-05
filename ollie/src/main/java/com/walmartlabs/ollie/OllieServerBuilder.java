package com.walmartlabs.ollie;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 Takari
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import java.io.File;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.walmartlabs.ollie.guice.*;
import com.walmartlabs.ollie.lifecycle.LifecycleRepository;
import org.apache.shiro.realm.Realm;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
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
  boolean databaseSupport = false;
  String changeLogFile = "liquibase.xml";
  String changeLogTableName = "DATABASE_CHANGE_LOG";
  String changeLogLockTableName = "DATABASE_CHANGE_LOG_LOCK";
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

  boolean sessionsEnabled = false;
  int sessionMaxInactiveInterval = -1;

  boolean sslEnabled = false;
  Set<SessionCookieOptions> sessionCookieOptions = Collections.emptySet();
  List<FilterDefinition> filterDefintions = Lists.newArrayList();
  List<ServletDefinition> servletDefinitions = Lists.newArrayList();
  List<StaticResourceDefinition> staticContentDefinitions = Lists.newArrayList();
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

  boolean jmxEnabled = false;

  OllieSecurityModuleProvider securityModuleProvider;
  Set<OllieShutdownListener> shutdownListeners = Sets.newHashSet();
  private String webServletsPath;

  public OllieServer build() {
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

  public OllieServerBuilder databaseSupport() {
    this.databaseSupport = true;
    return this;
  }

  public OllieServerBuilder databaseSupport(String changeLogFile) {
    this.databaseSupport = true;
    this.changeLogFile = changeLogFile;
    return this;
  }

  public OllieServerBuilder databaseSupport(String changeLogFile, String logTableName, String lockTableName) {
    this.databaseSupport = true;
    this.changeLogFile = changeLogFile;
    this.changeLogTableName = logTableName;
    this.changeLogLockTableName = lockTableName;
    return this;
  }

  public boolean hasDBSupport() {
    return this.databaseSupport;
  }

  public String changeLogFile() { return this.changeLogFile; }

  public String changeLogTableName() { return this.changeLogTableName; }

  public String changeLogLockTableName() { return this.changeLogLockTableName; }
  
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

  public OllieServerBuilder sessionMaxInactiveInterval(int sessionMaxInactiveInterval) {
    this.sessionMaxInactiveInterval = sessionMaxInactiveInterval;
    return this;
  }

  public boolean sslEnabled() {
    return sslEnabled;
  }
  
  public OllieServerBuilder sslEnabled(boolean sslEnabled) {
    this.sslEnabled = sslEnabled;
    return this;
  }

  public Set<SessionCookieOptions> sessionCookieOptions() {
    return sessionCookieOptions;
  }

  public OllieServerBuilder sessionCookieOptions(Set<SessionCookieOptions> sessionCookieOptions) {
    this.sessionCookieOptions = sessionCookieOptions;
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

  public OllieServerBuilder jmxEnabled(boolean jmxEnabled) {
    this.jmxEnabled = jmxEnabled;
    return this;
  }

  public OllieServerBuilder shutdownListener(OllieShutdownListener listener) {
    shutdownListeners.add(listener);
    return this;
  }

  public Set<OllieShutdownListener> shutdownListeners() {
    return shutdownListeners;
  }

  public OllieSecurityModuleProvider securityModuleProvider() {
    return this.securityModuleProvider;
  }

  public OllieServerBuilder securityModuleProvider(OllieSecurityModuleProvider securityModuleProvider) {
    this.securityModuleProvider = securityModuleProvider;
    return this;
  }

  public File secrets() {
    return secrets;
  }

  public OllieServerBuilder serveWebServlets(String webServletsPath) {
    this.webServletsPath = webServletsPath;
    return this;
  }

  public String webServletsPath() {
    return webServletsPath;
  }
}
