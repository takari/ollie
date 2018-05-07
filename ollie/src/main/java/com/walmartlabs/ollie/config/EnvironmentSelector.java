package com.walmartlabs.ollie.config;

public class EnvironmentSelector {
  
  public final static String ENVIRONMENT_KEY = "ollie.environment";
  
  public Environment select() {
    String environmentName = System.getProperty(ENVIRONMENT_KEY, Environment.DEVELOPMENT.id());
    return Environment.fromName(environmentName);
  }
}
