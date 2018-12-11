package com.walmartlabs.ollie.guice;

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

import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.walmartlabs.ollie.OllieServerBuilder;
import com.walmartlabs.ollie.model.FilterDefinition;

public class OllieSecurityModule extends AbstractModule {

  private static Logger logger = LoggerFactory.getLogger(OllieServletModule.class);

  private final OllieServerBuilder serverConfiguration;
  private final ServletContext servletContext;
  
  public OllieSecurityModule(OllieServerBuilder config, ServletContext servletContext) {
    this.serverConfiguration = config;
    this.servletContext = servletContext;
  }

  @Override
  protected void configure() {
    if (serverConfiguration.realms() != null) {
      install(new SecurityWebModule(servletContext));
      install(new SecurityAnnotationsModule());
      ShiroWebModule.bindGuiceFilter(binder()); // filter(/*).through(GuiceShiroFilter.class)
    }
  }

  public class SecurityWebModule extends ShiroWebModule {

    public SecurityWebModule(ServletContext servletContext) {
      super(servletContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configureShiroWeb() {

      // Shiro Realms
      for (Class<? extends Realm> realmClass : serverConfiguration.realms()) {
        logger.info("Installing Shiro realm: {}.", realmClass.getName());
        bindRealm().to(realmClass);
      }

      // Authentication Filters
      if (serverConfiguration.filterChains() != null) {
        for (FilterDefinition filterDefinition : serverConfiguration.filterChains()) {
          String[] patterns = filterDefinition.getPatterns();
          for (String pattern : patterns) {
            Class<? extends Filter> filterClass = filterDefinition.getFilterClass();
            logger.info("Installing authentication filter: {} -> {}.", pattern, filterClass.getName());
            addFilterChain(pattern, Key.get(filterClass));
          }
        }
      }
    }
  }

  // This module enables support for the standard Shiro annotations in addition to any custom interceptors.
  //
  // @RequiresAuthentication 
  // https://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/authz/annotation/RequiresAuthentication.html
  //
  // @RequiresGuest 
  // https://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/authz/annotation/RequiresGuest.html
  //
  // @RequiresPermissions 
  // https://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/authz/annotation/RequiresPermissions.html
  //
  // @RequiresRoles 
  // https://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/authz/annotation/RequiresRoles.html
  //
  // @RequiresUser 
  // https://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/authz/annotation/RequiresUser.html
  //
  class SecurityAnnotationsModule extends ShiroAopModule {
    @Override
    protected void configureInterceptors(AnnotationResolver resolver) {}
  }
}
