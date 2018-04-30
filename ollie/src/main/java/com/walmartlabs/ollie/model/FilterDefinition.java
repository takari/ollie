package com.walmartlabs.ollie.model;

import javax.servlet.Filter;

public class FilterDefinition {
  String pattern;
  Class<? extends Filter> filterClass;

  public FilterDefinition() {    
  }

  public FilterDefinition(String pattern, Class<? extends Filter> filterClass) {
    this.pattern = pattern;
    this.filterClass = filterClass;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public Class<? extends Filter> getFilterClass() {
    return filterClass;
  }

  public void setFilterClass(Class<? extends Filter> filterClass) {
    this.filterClass = filterClass;
  }

  @Override
  public String toString() {
    return "FilterDefinition [pattern=" + pattern + ", filterClass=" + filterClass + "]";
  }
}