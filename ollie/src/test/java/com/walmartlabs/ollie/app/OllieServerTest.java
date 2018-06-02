package com.walmartlabs.ollie.app;

import org.junit.Test;

import com.walmartlabs.ollie.OllieServer;

public class OllieServerTest {
  
  @Test
  public void validate() {
    OllieServer server = OllieServer.builder()
      .port(9000)
      .name("gatekeeper")
      .packageToScan("com.walmartlabs.ollie.app")
      .build();
    server.start();
  }
  
  public static void main(String[] args) {
    OllieServer server = OllieServer.builder()      
      .port(9000)
      .name("gatekeeper")
      .packageToScan("com.walmartlabs.ollie.app")
      .build();
    server.start();    
  }
}
