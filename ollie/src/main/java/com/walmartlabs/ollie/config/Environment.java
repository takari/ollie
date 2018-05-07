package com.walmartlabs.ollie.config;

public enum Environment {
  DEVELOPMENT("development"), QA("qa"), STAGE("staging"), PRODUCTION("production");

  private final String id;

  private Environment(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
  
  public static Environment fromName(String id) {
    if(DEVELOPMENT.id().equals(id)) {
      return DEVELOPMENT;
    } else if (QA.id().equals(id)) {
      return QA;
    } else if (STAGE.id().equals(id)) {
      return STAGE;
    } else if(PRODUCTION.id().equals(id)) {
      return PRODUCTION;
    }
    throw new RuntimeException(String.format("The environment with id of '%s' is not supported", id));
  }
}
