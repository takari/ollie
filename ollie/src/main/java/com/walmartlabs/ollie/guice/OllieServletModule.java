package com.walmartlabs.ollie.guice;

import org.sonatype.siesta.server.SiestaServlet;

import com.google.inject.servlet.ServletModule;
import com.walmartlabs.ollie.OllieServerBuilder;
import com.walmartlabs.ollie.config.OllieConfigurationModule;

public class OllieServletModule extends ServletModule {

  private final OllieServerBuilder serverConfiguration;

  public OllieServletModule(OllieServerBuilder config) {
    this.serverConfiguration = config;
  }
      
  @Override
  protected void configureServlets() {
    bind(OllieServerBuilder.class).toInstance(serverConfiguration);
    OllieJaxRsModule jaxRsModule = new OllieJaxRsModule(serverConfiguration);
    install(jaxRsModule);
    //TODO: clean up this mess
    serve(jaxRsModule.apiPatterns()[0] + "/*", jaxRsModule.morePatterns()).with(SiestaServlet.class, jaxRsModule.parameters());
    install(new OllieConfigurationModule(serverConfiguration));
    install(new OllieSecurityModule(serverConfiguration, getServletContext()));
  }
}
