package com.walmartlabs.ollie.config;

public enum Environment {
  DEVELOPMENT("development"), QA("qa"), STAGING("staging"), PRODUCTION("production");

  private final String identifier;

  Environment(String identifier) {
    this.identifier = identifier;
  }

  public String identifier() {
    return identifier;
  }
}
