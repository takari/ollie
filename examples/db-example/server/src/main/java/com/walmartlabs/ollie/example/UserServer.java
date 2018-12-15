package com.walmartlabs.ollie.example;

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
                .databaseSupport("liquibase.xml", "LOG_TABLE", "LOCK_TABLE");

        server = builder.build();
        server.start();
        logger.info("Server started on port 9000");
    }
}
