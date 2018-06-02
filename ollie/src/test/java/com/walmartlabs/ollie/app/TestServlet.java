package com.walmartlabs.ollie.app;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.DefaultServlet;

import com.google.inject.Singleton;
import com.walmartlabs.ollie.config.Config;

@Named
@Singleton
public class TestServlet extends DefaultServlet {

  private final String name;
  
  public TestServlet(@Config("name") String name) {
    this.name = name;
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try(PrintWriter out = response.getWriter()) {
      out.write(String.format("Hello %s.", name));
    }
  }
}
