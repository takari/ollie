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

import okhttp3.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class UserServerTest {

    private UserServer server;
    private String basedir;

    @Before
    public void beforeTest() {
        basedir = System.getProperty("basedir", new File("").getAbsolutePath());
        server = new UserServer();
        server.start();
    }

    @Test
    public void validUserServer() throws Exception {
        assertEquals(200, postTestResource("example/user", "user.json").code());
    }

    private Response postTestResource(String path, String resource) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(basedir, "src/test/resources", resource)));
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
        Request request = new okhttp3.Request.Builder().url("http://localhost:9000/api/" + path).post(body).build();
        return client.newCall(request).execute();
    }
}
