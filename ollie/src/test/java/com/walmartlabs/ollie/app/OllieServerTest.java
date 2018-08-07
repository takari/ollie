package com.walmartlabs.ollie.app;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.walmartlabs.ollie.OllieServer;

import java.io.File;

public class OllieServerTest {

  @Test
  public void validate() throws Exception {
    OllieServer server = server();
    executeStandardTests();
    server.stop();
  }

  protected void executeStandardTests() throws Exception {
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
  }

  protected String url(String path) {
    return String.format("http://localhost:9000%s", path);
  }

  protected static OllieServer server() {
    OllieServer server =
        OllieServer.builder()
            .port(9000)
            .name("testserver")
            .packageToScan("com.walmartlabs.ollie.app")
            .secrets(file("secrets.properties"))
            .serve("/testservlet")
            .with(
                TestServlet.class,
                ImmutableMap.of("servlet.config.string", "servlet-config-string"))
            .build();
    server.start();
    return server;
  }

  protected static File file(String path) {
    String basedir = new File("").getAbsolutePath();
    return new File(basedir, String.format("src/test/resources/%s", path));
  }

  public static void main(String[] args) {
    OllieServer server = server();
  }
}
