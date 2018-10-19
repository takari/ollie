package com.walmartlabs.ollie.example;

import com.walmartlabs.ollie.OllieServer;
import com.walmartlabs.ollie.OllieServerBuilder;
import com.walmartlabs.ollie.db.DatabaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServer {

    private final static Logger logger = LoggerFactory.getLogger(UserServer.class);

    private OllieServer server;

    public UserServer() {}

    public static void main(String[] args) throws Exception {
        new UserServer().start();
    }

    public void start() {
        logger.info("Starting server");
        OllieServerBuilder builder = new OllieServerBuilder()
                .port(9000)
                .name("userServer") //name of .conf file in src/main/resources
                .module(new DatabaseModule()) //wires DB configuration classes to injector
                .packageToScan("com.walmartlabs.ollie.example");

        server = builder.build();
        server.start();
        logger.info("Server started on port 9000");
    }
}
