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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmartlabs.ollie.config.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;

@Named
@Singleton
@WebServlet("/webservlet")
public class TestWebServlet extends HttpServlet {

    // Make sure components get @Injected correctly
    private final TestComponent testComponent;
    // Make sure configuration elements get @Injected correctly
    private final String stringConfig;

    @Inject
    public TestWebServlet(TestComponent testComponent, @Config("servlet.config.string") String stringConfig) {
        this.testComponent = testComponent;
        this.stringConfig = stringConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // We are outputting JSON so we can use RESTEasy's nice JSON parsing capabilities in tests
        response.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        try (PrintWriter out = response.getWriter()) {
            out.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(new TestOutput(testComponent.message(), stringConfig)));
        }
    }

    class TestOutput {

        String testComponentMessage;
        String stringConfig;

        public TestOutput(String testComponentMessage, String stringConfig) {
            this.testComponentMessage = testComponentMessage;
            this.stringConfig = stringConfig;
        }

        public String getTestComponentMessage() {
            return testComponentMessage;
        }

        public String getStringConfig() {
            return stringConfig;
        }
    }
}
