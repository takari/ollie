package com.walmartlabs.ollie.app;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 - 2019 Takari
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
import org.apache.shiro.authc.*;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertNotNull;

public class RealmsTest {

    private OllieServer server;

    @Before
    public void init() {
        OllieServer server = OllieServer.builder()
                .port(0)
                .name("realms")
                .packageToScan("com.walmartlabs.ollie.test.realms")
                .realm(TestRealm.class)
                .filterChain("/test/realms", TestAuthenticationFilter.class)
                .build();

        server.start();

        this.server = server;
    }

    @After
    public void tearDown() {
        if (this.server != null) {
            this.server.stop();
        }
    }

    @Test
    public void validate() throws Exception {
        // TODO request with basic auth
    }

    public static class TestRealm extends AuthenticatingRealm {

        @Override
        public boolean supports(AuthenticationToken token) {
            return token instanceof UsernamePasswordToken;
        }

        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
            UsernamePasswordToken t = (UsernamePasswordToken) authenticationToken;
            if (!"test".equalsIgnoreCase(t.getUsername())) {
                return null;
            }

            return new SimpleAccount(t, t, "test");
        }
    }

    public static class TestAuthenticationFilter extends AuthenticatingFilter {

        @Override
        protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

            assertNotNull(servletRequest.getServletContext());

            HttpServletRequest req = WebUtils.toHttp(servletRequest);

            String auth = req.getHeader("Authorization");
            if (auth == null) {
                return new UsernamePasswordToken();
            }

            return new UsernamePasswordToken(auth, "test");
        }

        @Override
        protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
            return executeLogin(servletRequest, servletResponse);
        }
    }
}
