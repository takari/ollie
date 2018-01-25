package io.takari.server.model;

import java.util.List;

import com.google.common.collect.Lists;

public class StaticResourceDefinition {
  String path;
  String resource;
  List<String> welcomeFiles = Lists.newArrayList();
  boolean listing;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public List<String> getWelcomeFiles() {
    return welcomeFiles;
  }

  public void setWelcomeFiles(List<String> welcomeFiles) {
    this.welcomeFiles = welcomeFiles;
  }

  public boolean isListing() {
    return listing;
  }

  public void setListing(boolean listing) {
    this.listing = listing;
  }

  @Override
  public String toString() {
    return "StaticResourceDefinition [path=" + path + ", resource=" + resource + ", welcomeFiles=" + welcomeFiles + ", listing=" + listing + "]";
  }
}