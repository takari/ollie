package com.walmartlabs.ollie.app;

import org.sonatype.siesta.Resource;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Named
@Singleton
@Path("/serialization")
public class SerializationTestResource implements Resource {

    @GET
    @Path("/emptyObject")
    @Produces(APPLICATION_JSON)
    public SingleValuePojo test() {
        return new SingleValuePojo();
    }
}
