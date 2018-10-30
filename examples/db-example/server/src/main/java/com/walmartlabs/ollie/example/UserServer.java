package com.walmartlabs.ollie.example;

import com.walmartlabs.ollie.OllieServer;
import com.walmartlabs.ollie.OllieServerBuilder;
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
                .packageToScan("com.walmartlabs.ollie.example")
                .databaseSupport();

        server = builder.build();
        server.start();
        logger.info("Server started on port 9000");
    }
}
