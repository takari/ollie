package com.walmartlabs.ollie.app;

public class TestEntity {

  private final String name;
  private final String stringConfig;
  private final int integerConfig;
  private final float floatConfig;
  private final String jiraPassword;

  public TestEntity(String name, String stringConfig, int integerConfig, float floatConfig, String jiraPassword) {
    this.name = name;
    this.stringConfig = stringConfig;
    this.integerConfig = integerConfig;
    this.floatConfig = floatConfig;
    this.jiraPassword = jiraPassword;
  }

  public String getName() {
    return name;
  }

  public String getStringConfig() {
    return stringConfig;
  }

  public int getIntegerConfig() {
    return integerConfig;
  }

  public float getFloatConfig() {
    return floatConfig;
  }

  public String getJiraPassword() {
    return jiraPassword;
  }
}
