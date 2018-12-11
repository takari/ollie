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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//
// What we have commented out is the way we would ideally like servlets to work. Using dependency injection
// and getting configuration from our own mechanism instead of init parameters.
//
//@Named
//@Singleton
public class TestServlet extends HttpServlet {

  private /*final*/ String stringConfig;

  /*
  @Inject
  public TestServlet(
    @Config("servlet.config.string") String stringConfig, TestComponent component) {    
    this.stringConfig = stringConfig;
  }
  */

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try (PrintWriter out = response.getWriter()) {
      out.write(stringConfig);
    }
  }
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    String stringConfig = config.getInitParameter("servlet.config.string");
    this.stringConfig = stringConfig;
  }
}
