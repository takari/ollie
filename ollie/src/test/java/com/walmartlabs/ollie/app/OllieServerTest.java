package com.walmartlabs.ollie.app;

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
