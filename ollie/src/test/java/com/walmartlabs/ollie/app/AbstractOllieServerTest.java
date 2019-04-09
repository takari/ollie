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

import com.google.common.collect.ImmutableMap;
import com.walmartlabs.ollie.OllieServer;
import com.walmartlabs.ollie.OllieServerBuilder;
import org.junit.After;
import org.junit.Before;

import java.io.File;

public abstract class AbstractOllieServerTest {

  protected OllieServer server;
  protected OllieServerBuilder builder;

  @Before
  public void init() {
    server = server();
  }

  @After
  public void tearDown() {
    if (server != null) {
      server.stop();
    }
  }

  protected String url(String path) {
    int port = server.port();
    return String.format("http://localhost:%s%s", port, path);
  }

  protected static File file(String path) {
    String basedir = new File("").getAbsolutePath();
    return new File(basedir, String.format("src/test/resources/%s", path));
  }

  private OllieServer server() {
    builder = new OllieServerBuilder();
    OllieServer server =
        builder
            .port(0)
            .name("testserver")
            .packageToScan("com.walmartlabs.ollie.app")
            .secrets(file("secrets.properties"))
            .serve("/testservlet")
            .with(
                TestServlet.class,
                ImmutableMap.of("servlet.config.string", "servlet-config-string"))
            .build();
    server.start();
    return server;
  }
}
