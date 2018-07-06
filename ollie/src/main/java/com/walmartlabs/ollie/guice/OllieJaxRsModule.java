package com.walmartlabs.ollie.guice;

import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.siesta.server.resteasy.ResteasyModule;
import org.sonatype.siesta.server.validation.ValidationModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.walmartlabs.ollie.OllieServerBuilder;

public class OllieJaxRsModule extends AbstractModule {

  private final static Logger logger = LoggerFactory.getLogger(OllieJaxRsModule.class);
  private final OllieServerBuilder serverConfiguration;
  private String[] apiPatterns;
  private String[] morePatterns;
  private final Map<String, String> parameters;

  public OllieJaxRsModule(OllieServerBuilder serverConfiguration) {
    this.serverConfiguration = serverConfiguration;
    apiPatterns = serverConfiguration.apiPatterns();
    String resteasyPrefix = serverConfiguration.api();
    if (apiPatterns == null) {
      apiPatterns = new String[] {serverConfiguration.api()};
    } else {
      resteasyPrefix = "/";
    }

    if (apiPatterns.length == 0) {
      throw new IllegalArgumentException("'apiPatterns' should contain at least one pattern");
    }
    morePatterns = apiPatterns.length > 1 ? Arrays.copyOfRange(apiPatterns, 1, apiPatterns.length) : new String[0];
    parameters = ImmutableMap.of("resteasy.servlet.mapping.prefix", resteasyPrefix);
  }

  public String[] apiPatterns() {
    return apiPatterns;
  }
  
  public String[] morePatterns() {
    return morePatterns;
  }
  
  public Map<String,String> parameters() {
    return parameters;
  }

  @Override
  protected void configure() {
    JaxRsClasses resourcesHolder = new JaxRsClasses();
    if (serverConfiguration.docs() != null) {
      // Collect all JAXRS classes so they can be used to generate Swagger documentation or any other processing.
      bindListener(Matchers.any(), new TypeListener() {
        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
          if (type.getRawType().getAnnotation(Path.class) != null && !type.getRawType().equals(ApiDocsResource.class)) {
            logger.info("Registering JAXRS resource {}.", type.getRawType());
            resourcesHolder.jaxRsClass(type.getRawType());
          }
        }
      });
      bind(JaxRsClasses.class).toInstance(resourcesHolder);
    }

    // RESTEasy JAXRS
    install(new ResteasyModule());
    install(new ValidationModule());
  }
}
