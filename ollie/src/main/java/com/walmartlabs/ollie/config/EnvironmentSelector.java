package com.walmartlabs.ollie.config;

import java.io.File;

public class EnvironmentSelector {
  
  public final static String ENVIRONMENT_KEY = "ollie.environment";
  public final static File OLLIE_DIRECTORY = new File(System.getProperty("user.home"), ".ollie");
  
  public Environment select() {    
    String environmentName;
    if(OLLIE_DIRECTORY.exists()) {
      environmentName = Environment.DEVELOPMENT.id();
    }
    environmentName = System.getProperty(ENVIRONMENT_KEY, Environment.DEVELOPMENT.id());    
    return Environment.fromName(environmentName);
  }
}
