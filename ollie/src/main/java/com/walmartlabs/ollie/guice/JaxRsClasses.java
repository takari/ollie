package com.walmartlabs.ollie.guice;

import java.util.Set;

import com.google.common.collect.Sets;

public class JaxRsClasses {

  private Set<Class<?>> jaxRsClasses = Sets.newConcurrentHashSet();
  
  public void jaxRsClass(Class<?> jaxRsClass) {
    jaxRsClasses.add(jaxRsClass);
  }
  
  public Set<Class<?>> get() {
    return jaxRsClasses;
  }
}
