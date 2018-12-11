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

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.walmartlabs.ollie.OllieServer;

import java.io.File;

public class OllieServerTest extends AbstractOllieServerTest {

  @Test
  public void validate() {
    // TestResource
    when()
        .get(url("/api/test?name=ollie"))
        .then()
        .body(
            "name",
            equalTo("ollie"),
            "stringConfig",
            equalTo("resource-config-string"),
            "integerConfig",
            equalTo(1),
            "floatConfig",
            equalTo(2.5f),
            "jiraPassword",
            equalTo("super-secret"));

    // TestServlet
    when().get(url("/testservlet")).then().body(containsString("servlet-config-string"));
    // Swagger
    when().get(url("/api/docs")).then().body("swagger", equalTo("2.0"));
  }
}
