package com.walmartlabs.ollie.app;

import org.junit.Test;

import com.walmartlabs.ollie.WebServer;
import com.walmartlabs.ollie.guice.JaxRsServerBuilder;

public class JaxRsServerTest {
  
  @Test
  public void validate() {
    WebServer server = new JaxRsServerBuilder()
      .port(9000)
      .name("gatekeeper")
      .packageToScan("io.takari.server.app")      
      .build();
    server.start();
  }
  
  public static void main(String[] args) {
    WebServer server = new JaxRsServerBuilder()      
      .port(9000)
      .name("gatekeeper")
      .packageToScan("io.takari.server.app")
      .build();
    server.start();    
  }
}
