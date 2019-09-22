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
import org.apache.shiro.authc.*;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.File;

import static org.junit.Assert.assertNotNull;

public class TestServerBuilder {

  protected static File file(String path) {
    String basedir = new File("").getAbsolutePath();
    return new File(basedir, String.format("src/test/resources/%s", path));
  }

  public OllieServer build() {
    OllieServer server =
        new OllieServerBuilder()
            .port(0)
            .sessionsEnabled(true)
            .name("testserver")
            .packageToScan("com.walmartlabs.ollie.app")
            .secrets(file("secrets.properties"))
            .realm(TestRealm.class)
            .filterChain("/test/realms/**", NoSessionCreationFilter.class)
            .filterChain("/test/realms/**", BasicHttpAuthenticationFilter.class)
            .serve("/test/realms/secret")
            .with(TestServletBehindAuth.class)
            .serve("/testservlet")
            .with(
                TestServlet.class,
                ImmutableMap.of("servlet.config.string", "servlet-config-string"))
            .build();
    server.start();
    return server;
  }

  public static class TestRealm extends AuthenticatingRealm {

    @Override
    public boolean supports(AuthenticationToken token) {
      return token instanceof UsernamePasswordToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
        throws AuthenticationException {
      UsernamePasswordToken t = (UsernamePasswordToken) authenticationToken;
      if (!"test".equalsIgnoreCase(t.getUsername()) && "test".equalsIgnoreCase((String)t.getCredentials())) {
        return null;
      }
      return new SimpleAccount(t.getUsername(), t.getCredentials(), "test");
    }
  }

  public static void main(String[] args) throws Exception {
    OllieServer server = new TestServerBuilder().build();
    server.start();
  }
}
