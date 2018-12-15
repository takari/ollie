package com.walmartlabs.ollie.app;

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
