package com.walmartlabs.ollie.config;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;

public class ConfigurationProcessor {

  private final String name;
  private final String environment;
  private final File overridesFile;

  public ConfigurationProcessor(String name, String environment) {
    this(name, environment, null);
  }

  public ConfigurationProcessor(String name, String environment, File overridesFile) {
    this.name = name;
    this.environment = environment;
    this.overridesFile = overridesFile;
  }

  public com.typesafe.config.Config process() {
    Config configuration = ConfigFactory.load(name + ".conf", ConfigParseOptions.defaults(), ConfigResolveOptions.noSystem());
    System.out.println(configuration);
    //System.out.println(configuration.getString("c"));
    Config applicationConfiguration = configuration.getConfig(name);    
    Config environmentConfiguration = applicationConfiguration.getConfig(environment);     
    Config result = environmentConfiguration.withFallback(applicationConfiguration);
    //
    // For development we want an easy way to plug in values without having to modify resources
    // in source control. Developers can define configuration outside of source control and have
    // them automatically merged into the configuration during local testing.
    //
    Config overrides; 
    if (overridesFile != null) {
      overrides = ConfigFactory.parseFile(overridesFile);
      return overrides.withFallback(result);
    } else {
      return result;      
    }
  }
}
