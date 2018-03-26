package com.walmartlabs.ollie.config;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.walmartlabs.ollie.config.ConfigurationProcessor;

public class ConfigurationProcessorTest {

  private String basedir;

  @Before
  public void setUp() {
    basedir = System.getProperty("basedir", new File("").getAbsolutePath());
  }

  @Test
  public void validateConfigurationProcessorUsingOverridesFile() {
    File overridesFile = new File(basedir, "src/test/resources/overrides.conf");
    ConfigurationProcessor processor = new ConfigurationProcessor("gatekeeper", "development", overridesFile);
    com.typesafe.config.Config config = processor.process();
    assertEquals("http://adapter.server.com:8080/adapter/rest/dj/simple/approvals", config.getString("approver.settle.url"));
    assertEquals("https://jira.server.com", config.getString("jira.server"));
    //
    // These come from the local overrides file because we don't want this sensitive information checked into source control
    //
    assertEquals("settletoken", config.getString("approver.settle.token"));    
    assertEquals("username", config.getString("jira.username"));
    assertEquals("password", config.getString("jira.password"));
  }
}
