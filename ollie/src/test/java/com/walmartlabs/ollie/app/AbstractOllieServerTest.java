package com.walmartlabs.ollie.app;

import com.google.common.collect.ImmutableMap;
import com.walmartlabs.ollie.OllieServer;
import org.junit.After;
import org.junit.Before;

import java.io.File;

public abstract class AbstractOllieServerTest {

  protected OllieServer server;

  @Before
  public void init() {
    server = server();
  }
  
  @After
  public void tearDown() {
    if (server != null) {
      server.stop();
    }
  }
  
  protected String url(String path) {
    int port = server.port();
    return String.format("http://localhost:%s%s", port, path);
  }

  protected static File file(String path) {
    String basedir = new File("").getAbsolutePath();
    return new File(basedir, String.format("src/test/resources/%s", path));
  }

  private static OllieServer server() {
    OllieServer server =
        OllieServer.builder()
            .port(0)
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
}
