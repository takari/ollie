package com.walmartlabs.ollie.model;

import javax.servlet.Filter;

public class FilterDefinition {
  String[] patterns;
  Class<? extends Filter> filterClass;

  public FilterDefinition() {    
  }

  public FilterDefinition(String[] patterns, Class<? extends Filter> filterClass) {
    this.patterns = patterns;
    this.filterClass = filterClass;
  }

  public String[] getPatterns() {
    return patterns;
  }

  public void setPatterns(String... patterns) {
    this.patterns = patterns;
  }

  public Class<? extends Filter> getFilterClass() {
    return filterClass;
  }

  public void setFilterClass(Class<? extends Filter> filterClass) {
    this.filterClass = filterClass;
  }

  @Override
  public String toString() {
    return "FilterDefinition [patterns=" + patterns + ", filterClass=" + filterClass + "]";
  }
}