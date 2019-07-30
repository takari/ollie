package com.walmartlabs.ollie.config;

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

import com.typesafe.config.*;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationProcessor {

  public static final String CONFIG_FILE = "ollie.conf";
  private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);

  private final String name;
  private final Environment environment;
  private final File overridesFile;
  private final File secretsProperties;

  public ConfigurationProcessor(String name) {
    this(name, Environment.DEVELOPMENT, null, null);
  }

  public ConfigurationProcessor(String name, File overridesFile) {
    this(name, Environment.DEVELOPMENT, overridesFile, null);
  }

  public ConfigurationProcessor(String name, Environment environment) {
    this(name, environment, null, null);
  }

  public ConfigurationProcessor(
      String name, Environment environment, File overridesFile, File secretsProperties) {
    this.name = name;
    this.environment = environment;
    this.overridesFile = overridesFile;
    this.secretsProperties = secretsProperties;
  }

  public com.typesafe.config.Config process() {
    String configurationName = name + ".conf";
    logger.info("Processing configuration resource {}", configurationName);
    ConfigResolveOptions resolveOptions = ConfigResolveOptions.defaults().setAllowUnresolved(true);
    Config configuration = ConfigFactory.load(configurationName, ConfigParseOptions.defaults(), resolveOptions);
    Config result = configuration.getConfig(name);

    // If a configuration file is specified then use it
    if (System.getProperty(CONFIG_FILE) != null) {
      String p = System.getProperty(CONFIG_FILE);
      logger.info("Processing configuration file {}", p);
      Config externalConfiguration = ConfigFactory.parseFile(new File(p)).getConfig(name);
      result = externalConfiguration.withFallback(result);
    }

    // Take the configuration for the specified Environment
    Config environmentConfiguration = result.getConfig(environment.id());

    // Now that we have the configuration for the specified environment let's remove
    // the configuration for all the environments so we are left with the top-level
    // configuration elements that are shared across all the environments
    for(Environment e : Environment.values()) {
      result = result.withoutPath(e.id());
    }

    // Now we will create a configuration merging the environment specific configuration and
    // the top-level shared configuration elements.
    result = environmentConfiguration.withFallback(result);

    //
    // For development we want an easy way to plug in values without having to modify resources
    // in source control. Developers can define configuration outside of source control and have
    // them automatically merged into the configuration during local testing.
    //
    // We assume that the overrides configuration has the same format as the application
    // configuration.
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
    if (environment == Environment.DEVELOPMENT && overridesFile != null) {
      if (!overridesFile.exists()) {
        throw new RuntimeException(
            String.format(
                "The specified overrides configuration doesn't exist: '%s'.", overridesFile));
      }
      overrides = ConfigFactory.parseFile(overridesFile);
      try {
        overrides = overrides.getConfig(name);
      } catch (ConfigException e) {
        throw new RuntimeException(
            String.format(
                "The specified application '%s' is not present in the overrides file '%s'.",
                name, overridesFile));
      }
      try {
        overrides = overrides.getConfig(environment.id());
      } catch (ConfigException e) {
        throw new RuntimeException(
            String.format(
                "The specified environment '%s' is not present in the application configuration '%s' in the overrides file '%s'.",
                environment.id(), name, overridesFile));
      }
      result = overrides.withFallback(result);
    }

    if(secretsProperties != null && secretsProperties.exists()) {
      Config secrets = ConfigFactory.parseFile(secretsProperties);
      result = result.resolveWith(secrets);
    }

    return result.resolve();
  }

  // Eventually we might do this to perform more complicated lookups in remote systems
  private static ConfigResolver secretsResolver =
      new ConfigResolver() {

        @Override
        public ConfigValue lookup(String path) {
          return null;
        }

        @Override
        public ConfigResolver withFallback(ConfigResolver fallback) {
          return fallback;
        }
      };
}
