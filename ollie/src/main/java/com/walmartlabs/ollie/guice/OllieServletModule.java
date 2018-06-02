package com.walmartlabs.ollie.guice;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.siesta.server.SiestaServlet;
import org.sonatype.siesta.server.resteasy.ResteasyModule;
import org.sonatype.siesta.server.validation.ValidationModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.walmartlabs.ollie.config.ConfigurationModule;
import com.walmartlabs.ollie.config.ConfigurationProcessor;
import com.walmartlabs.ollie.config.Environment;
import com.walmartlabs.ollie.config.EnvironmentSelector;
import com.walmartlabs.ollie.model.FilterDefinition;

public class OllieServletModule extends ServletModule {

  private static Logger logger = LoggerFactory.getLogger(OllieServletModule.class);
  private final OllieServerBuilder serverConfiguration;

  public OllieServletModule(OllieServerBuilder config) {
    this.serverConfiguration = config;
  }

  @Override
  protected void configureServlets() {

    bind(OllieServerBuilder.class).toInstance(serverConfiguration);

    JaxRsClasses resourcesHolder = new JaxRsClasses();
    if (serverConfiguration.docs() != null) {
      // Collect all JAXRS classes so they can be used to generate Swagger documentation or any other processing.
      bindListener(Matchers.any(), new TypeListener() {
        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
          if (type.getRawType().getAnnotation(Path.class) != null && !type.getRawType().equals(ApiDocsResource.class)) {
            resourcesHolder.jaxRsClass(type.getRawType());
          }
        }
      });
      bind(JaxRsClasses.class).toInstance(resourcesHolder);
    }

    // RESTEasy JAXRS
    install(new ResteasyModule());
    install(new ValidationModule());
    serve(serverConfiguration.api() + "/*").with(SiestaServlet.class, ImmutableMap.of("resteasy.servlet.mapping.prefix", serverConfiguration.api()));

    // Configuration: should be moved entirely into the module
    // strategies for determining environment
    EnvironmentSelector environmentSelector = new EnvironmentSelector();
    ConfigurationProcessor cp = new ConfigurationProcessor(serverConfiguration.name(), environmentSelector.select());
    install(ConfigurationModule.fromConfigWithPackage(cp.process(), serverConfiguration.packageToScan()));

    if (serverConfiguration.realms() != null) {
      install(new SecurityWebModule(getServletContext()));
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
