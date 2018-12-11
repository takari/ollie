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
