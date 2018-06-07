package com.walmartlabs.ollie.guice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.siesta.Resource;

import com.google.common.collect.ImmutableList;

import io.swagger.jaxrs.Reader;
import io.swagger.models.Info;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;

@Named
@Singleton
@Path("/docs")
public class ApiDocsResource implements Resource {
    private final Swagger swagger;
    private static Logger logger = LoggerFactory.getLogger(ApiDocsResource.class);

    @Inject
    public ApiDocsResource(JaxRsClasses holder, OllieServerConfiguration config) {
        swagger = new Swagger();
        swagger.setSchemes(ImmutableList.of(Scheme.forValue("http")));
        swagger.setHost(getHost()+":"+config.port());
        swagger.setBasePath(config.api());

        Info info = new Info();
        info.setVersion("1.0.0");
        info.setTitle(config.title());
        info.setDescription(config.description());
        swagger.setInfo(info);

        Reader reader = new Reader(swagger);
        reader.read(holder.get());
    }

    private String getHost() {
        String host = "localhost";
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
            if(ip!=null && ip.getHostAddress()!=null && ip.getHostAddress().length()!=0 )
                host = ip.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("Failed to get the hostname. Using localhost as hostname for Swagger documentation.", e.getMessage());
        }
        return host;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceListing() throws IOException {
        return Response.ok().entity(swagger).build();
    }

}