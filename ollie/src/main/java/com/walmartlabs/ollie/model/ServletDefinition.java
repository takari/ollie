package com.walmartlabs.ollie.model;

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

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.security.SecurityHandler;

import com.google.common.collect.Maps;

public class ServletDefinition {
  String pattern;
  private Class<? extends HttpServlet> servletClass;
  private Map<String, String> parameters = Maps.newHashMap();
  private HttpServlet servlet;
  private File war;
  private SecurityHandler securityHandler;  

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public Class<? extends HttpServlet> getServletClass() {
    return servletClass;
  }

  public void setServletClass(Class<? extends HttpServlet> servletClass) {
    this.servletClass = servletClass;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  public void setServlet(HttpServlet servlet) {
    this.servlet = servlet;
  }
  
  public HttpServlet getServlet() {
    return servlet;
  }

  public File getWar() {
    return war;
  }

  public void setWar(File warFile) {
    this.war = warFile;
  }

  public void setSecurityHandler(SecurityHandler securityHandler) {
    this.securityHandler = securityHandler;
  }
  
  public SecurityHandler getSecurityHandler() {
    return securityHandler;
  }
}
