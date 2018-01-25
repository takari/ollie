package io.takari.server;

import org.junit.Test;

import io.takari.server.guice.JaxRsServerBuilder;

public class JaxRsServerTest {
  
  @Test
  public void validate() {
    WebServer server = new JaxRsServerBuilder()
      .port(9000)
      .build();
    server.start();
  }
  
  public static void main(String[] args) {
    WebServer server = new JaxRsServerBuilder()
      .port(9000)
      .build();
    server.start();    
  }
}
