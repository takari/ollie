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
