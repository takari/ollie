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
package io.takari.server;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Test resource.
 *
 * @since 2.0
 */
@Named
@Singleton
@Path("/ping")
@Api(value = "/ping", tags = "ping")
public class TestResource
  implements Resource {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @GET
  @Produces(TEXT_PLAIN)
  @ApiOperation(value = "Ping me!")
  public String ping(final @QueryParam("text") @DefaultValue("pong") String text) {
    if (log.isTraceEnabled()) {
      log.trace("PING", new Throwable("MARKER"));
    } else {
      log.debug("PING");
    }
    return text;
  }
}
