package com.walmartlabs.ollie.app;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 Takari
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.walmartlabs.ollie.lifecycle.Task;
import org.sonatype.siesta.Resource;

import com.walmartlabs.ollie.config.Config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

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
