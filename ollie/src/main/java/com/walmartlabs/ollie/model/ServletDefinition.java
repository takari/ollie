package com.walmartlabs.ollie.model;

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