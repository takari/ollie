package com.walmartlabs.ollie.config;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;

public class ConfigurationProcessor {

  public final static String CONFIG_FILE = "ollie.conf";

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
    String configurationName;
    if (System.getProperty(CONFIG_FILE) != null) {
      configurationName = System.getProperty(CONFIG_FILE);
    } else {
      configurationName = name + ".conf";
    }
    Config configuration = ConfigFactory.load(configurationName, ConfigParseOptions.defaults(), ConfigResolveOptions.noSystem());
    Config applicationConfiguration = configuration.getConfig(name);
    Config environmentConfiguration = applicationConfiguration.getConfig(environment);
    Config result = environmentConfiguration.withFallback(applicationConfiguration);
    //
    // For development we want an easy way to plug in values without having to modify resources
    // in source control. Developers can define configuration outside of source control and have
    // them automatically merged into the configuration during local testing.
    //
    // We assume that the overrides configuration has the same format as the application configuration.
    // So if an application configuration has a structure like the following:
    //
    // gatekeeper {                                                                                                                    
    //   development {                                                                                                                 
    //     approver.settle.token = "settletoken"                                                                                       
    //     jira.username = "username"                                                                                                  
    //     jira.password = "password"                                                                                                  
    //   }                                                                                                                             
    // }
    //
    // The we assume the overrides configuration has the same structure and the system will error
    // out if the structure is not the same.
    //     
    Config overrides;
    if (overridesFile != null) {
      if (!overridesFile.exists()) {
        throw new RuntimeException(String.format("The specified overrides configuration doesn't exist: '%s'.", overridesFile));
      }
      overrides = ConfigFactory.parseFile(overridesFile);
      try {
        overrides = overrides.getConfig(name);
      } catch (ConfigException e) {
        throw new RuntimeException(String.format("The specified application '%s' is not present in the overrides file '%s'.", name, overridesFile));
      }
      try {
        overrides = overrides.getConfig(environment);
      } catch (ConfigException e) {
        throw new RuntimeException(
          String.format("The specified environment '%s' is not present in the application configuration '%s' in the overrides file '%s'.", environment, name, overridesFile));
      }
      return overrides.withFallback(result);
    } else {
      return result;
    }
  }
}
