package com.walmartlabs.ollie.example;

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
        //assertEquals(200, postTestResource("example/user", "user.json").code());
    }

    private Response postTestResource(String path, String resource) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(basedir, "src/test/resources", resource)));
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), content);
        Request request = new okhttp3.Request.Builder().url("http://localhost:9000/api/" + path).post(body).build();
        return client.newCall(request).execute();
    }
}
