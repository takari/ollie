package com.walmartlabs.ollie.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * A {@link BindingAnnotation} Used to annotate fields, constructors and provider methods.
 * 
 * @author jason
 */
@BindingAnnotation
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface Config {

  /**
   * @return the {@link Config} path to the configuration value.
   */
  String value();
}
