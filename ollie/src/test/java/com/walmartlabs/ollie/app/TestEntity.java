package com.walmartlabs.ollie.app;

public class TestEntity {

  private final String name;
  private final String stringConfig;
  private final int integerConfig;
  private final float floatConfig;

  public TestEntity(String name, String stringConfig, int integerConfig, float floatConfig) {
    this.name = name;
    this.stringConfig = stringConfig;
    this.integerConfig = integerConfig;
    this.floatConfig = floatConfig;
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
}
