package com.walmartlabs.ollie.app;

import org.junit.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

public class SerializationTest extends AbstractOllieServerTest {

    @Test
    public void testNulls() {
        // nulls are omitted
        when().get(url("/api/serialization/emptyObject")).then().body(containsString("{ }"));
    }
}
