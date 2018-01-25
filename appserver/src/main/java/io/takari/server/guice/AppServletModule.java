package io.takari.server.guice;

import javax.servlet.ServletContext;
import javax.ws.rs.Path;

import org.apache.shiro.aop.AnnotationResolver;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.guice.web.GuiceShiroFilter;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.sonatype.siesta.server.SiestaServlet;
import org.sonatype.siesta.server.resteasy.ResteasyModule;
import org.sonatype.siesta.server.validation.ValidationModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class AppServletModule extends ServletModule {

  private final JaxRsServerConfig config;

  public AppServletModule(JaxRsServerConfig config) {
    this.config = config;
  }

  @Override
  protected void configureServlets() {
    
    bind(JaxRsServerConfig.class).toInstance(config);    
    
    JaxRsClasses resourcesHolder = new JaxRsClasses();
    if (config.docs() != null) {
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
    serve(config.api() + "/*").with(SiestaServlet.class, ImmutableMap.of("resteasy.servlet.mapping.prefix", config.api()));

    // Shiro
    if (config.realm() != null) {
      install(new SecurityWebModule(getServletContext()));
      install(new SecurityAnnotationsModule());
      filter("/*").through(GuiceShiroFilter.class);
    }   
  }

  public class SecurityWebModule extends ShiroWebModule {

    public SecurityWebModule(ServletContext servletContext) {
      super(servletContext);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configureShiroWeb() {
      // The actual Realm implementation is left up to the client application
      bindRealm().toProvider(config.realm());
      addFilterChain("/logout", LOGOUT);
      // Probably don't need these specifically
      // addFilterChain(config.docs() + "/**", NO_SESSION_CREATION, ANON);
      // addFilterChain(config.api() + "/**", NO_SESSION_CREATION, AUTHC_BASIC);
      // Need to create a filter with the pluggable realm
      addFilterChain("/**", NO_SESSION_CREATION, AUTHC_BASIC);
    }
  }

  // This module enables support for the standard Shiro annotations which are as follows:
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
