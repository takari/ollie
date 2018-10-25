package com.walmartlabs.ollie.example;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.siesta.Resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

@Named
@Singleton
@Path("/example/user")
@Api(value = "/example/user", tags = "user")
public class UserListener implements Resource {

    private final static Logger logger = LoggerFactory.getLogger(UserListener.class);

    private final UserDao userDao;

    @Inject
    public UserListener(UserDao userDao) {
        this.userDao = userDao;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Deployment Notification")
    public Response addUser(User user) throws IOException {
        logger.info("Received user request: {}", user.toString());
        userDao.insert(user.getFirstName(), user.getLastName());
        logger.info("Saved user to DB.");
        return Response.ok().build();
    }
}
