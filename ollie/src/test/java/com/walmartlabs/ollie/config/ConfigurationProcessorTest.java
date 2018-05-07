package com.walmartlabs.ollie.config;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConfigurationProcessorTest {

  @Rule 
  public ExpectedException exception = ExpectedException.none();

  private String basedir;

  @Before
  public void setUp() {
    basedir = System.getProperty("basedir", new File("").getAbsolutePath());
  }

  @Test
  public void validateConfigurationProcessor() {
    ConfigurationProcessor processor = new ConfigurationProcessor("gatekeeper", "development");
    com.typesafe.config.Config config = processor.process();
    assertEquals("dev-settle-token", config.getString("approver.settle.token"));    
    assertEquals("dev-jira-username", config.getString("jira.username"));
    assertEquals("dev-jira-password", config.getString("jira.password"));    
    assertEquals("caring", config.getString("sharing"));    
  }  

  @Test
  public void validateConfigurationProcessorWhereConfigurationFileIsOverridenWithASystemProperty() {
    System.setProperty(ConfigurationProcessor.CONFIG_FILE, "different.conf");
    ConfigurationProcessor processor = new ConfigurationProcessor("gatekeeper", "development");
    com.typesafe.config.Config config = processor.process();
    assertEquals("for different folks", config.getString("different-strokes"));    
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

  @Test
  public void validateConfigurationProcessorUsingNonExistentOverrides() {
    File overridesFile = new File(basedir, "src/test/resources/overrides-non-existent.conf");
    ConfigurationProcessor processor = new ConfigurationProcessor("gatekeeper", "development", overridesFile);
    exception.expect(RuntimeException.class);
    exception.expectMessage(containsString("The specified overrides configuration doesn't exist:"));
    processor.process();
  }    

  @Test
  public void validateConfigurationProcessorUsingOverridesFileReportsWrongStructureNoApplication() {
    File overridesFile = new File(basedir, "src/test/resources/overrides-wrong-structure-no-application.conf");
    ConfigurationProcessor processor = new ConfigurationProcessor("gatekeeper", "development", overridesFile);
    exception.expect(RuntimeException.class);
    exception.expectMessage(containsString("The specified application 'gatekeeper' is not present"));
    processor.process();
  }    

  @Test
  public void validateConfigurationProcessorUsingOverridesFileReportsWrongStructureNoEnvironment() {
    File overridesFile = new File(basedir, "src/test/resources/overrides-wrong-structure-no-environment.conf");
    ConfigurationProcessor processor = new ConfigurationProcessor("gatekeeper", "development", overridesFile);
    exception.expect(RuntimeException.class);
    exception.expectMessage(containsString("The specified environment 'development' is not present"));
    processor.process();
  }      
}
