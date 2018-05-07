package com.walmartlabs.ollie.app;

import org.junit.Test;

import com.walmartlabs.ollie.WebServer;
import com.walmartlabs.ollie.guice.OllieServerBuilder;

public class OllieServerTest {
  
  @Test
  public void validate() {
    WebServer server = new OllieServerBuilder()
      .port(9000)
      .name("gatekeeper")
      .packageToScan("com.walmartlabs.ollie.app")      
      .build();
    server.start();
  }
  
  public static void main(String[] args) {
    WebServer server = new OllieServerBuilder()      
      .port(9000)
      .name("gatekeeper")
      .packageToScan("com.walmartlabs.ollie.app")
      .build();
    server.start();    
  }
}
