/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.walmartlabs.ollie.app;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.siesta.Resource;

import com.walmartlabs.ollie.config.Config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Named
@Singleton
@Path("/ping")
@Api(value = "/ping", tags = "ping")
public class TestResource
  implements Resource {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String stringValue;
  private final int integerValue;
  private final float floatValue;
  
  @Inject
  public TestResource(
    @Config("app.string") String stringValue, 
    @Config("app.integer") int integerValue, 
    @Config("app.float") float floatValue) {
    
    this.stringValue = stringValue;
    this.integerValue = integerValue;
    this.floatValue = floatValue;
  }

  @GET
  @Produces(TEXT_PLAIN)
  @ApiOperation(value = "Ping me!")
  public String ping(final @QueryParam("text") @DefaultValue("pong") String text) {
    if (log.isTraceEnabled()) {
      log.trace("PING", new Throwable("MARKER"));
    } else {
      log.debug("PING");
    }
    return text + " with " + this;
  }

  @Override
  public String toString() {
    return "TestResource [stringValue=" + stringValue + ", integerValue=" + integerValue + ", floatValue=" + floatValue + "]";
  }
  
  
}
