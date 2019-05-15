package com.walmartlabs.ollie.guice;

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

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.siesta.Resource;

import com.google.common.collect.ImmutableList;
import com.walmartlabs.ollie.OllieServerBuilder;

import io.swagger.jaxrs.Reader;
import io.swagger.models.Info;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

@Named
@Singleton
@Path("/docs")
public class ApiDocsResource implements Resource {
  
  private final Swagger swagger;

  @Inject
  public ApiDocsResource(JaxRsClasses holder, HttpServletRequest request, OllieServerBuilder config) {
    swagger = new Swagger();
    swagger.setSchemes(ImmutableList.of(Scheme.forValue(request.getScheme())));
    swagger.setHost(String.format("%s:%s", request.getServerName(), request.getLocalPort()));
    swagger.setBasePath(config.api());
    
    Info info = new Info();    
    info.setVersion("1.0.0");
    info.setTitle(config.title());
    info.setDescription(config.description());
    swagger.setInfo(info);

    Reader reader = new Reader(swagger);
    reader.read(holder.get());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getServiceListing() {
    return Response.ok().entity(swagger).build();
  }
}
