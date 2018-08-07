package com.walmartlabs.ollie.app;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.sonatype.siesta.Resource;

import com.walmartlabs.ollie.config.Config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Named
@Singleton
@Path("/test")
@Api(value = "/test", tags = "test")
public class TestResource
  implements Resource {

  private final String stringConfig;
  private final int integerConfig;
  private final float floatConfig;
  private final String jiraPassword;

  @Inject
  public TestResource(
    //TODO: this should use bindings created for the test and not have to read a configuration file. Makes it hard to understand
    //      what's happening during the test.
    @Config("resource.config.string") String stringConfig,
    @Config("resource.config.integer") int integerConfig,
    @Config("resource.config.float") float floatConfig,
    @Config("jira.password") String jiraPassword) {

    this.stringConfig = stringConfig;
    this.integerConfig = integerConfig;
    this.floatConfig = floatConfig;
    this.jiraPassword = jiraPassword;
  }

  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Test Resource")
  public TestEntity ping(final @QueryParam("name") String name) {
    return new TestEntity(name, stringConfig, integerConfig, floatConfig, jiraPassword);
  }
}
