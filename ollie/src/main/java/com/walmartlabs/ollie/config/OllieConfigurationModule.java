package com.walmartlabs.ollie.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.*;
import com.google.inject.Module;
import com.typesafe.config.*;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

//import com.typesafe.config.Config;
import com.walmartlabs.ollie.OllieServerBuilder;

/**
 * Include this {@link Module} in your {@link Guice} bootstrapping to Automagically
 * get bindings to your {@link Config} annotated configuration parameters.
 * 
 * @author jason
 */
public class OllieConfigurationModule extends AbstractModule {

  private static final Provider<Object> NULL_PROVIDER = () -> null;

  private final com.typesafe.config.Config config;
  private final Reflections reflections;
  private final Set<Config> boundAnnotations;

  public OllieConfigurationModule(String packageToScan, com.typesafe.config.Config config) {
    ConfigurationBuilder configBuilder =
      new ConfigurationBuilder()
        .filterInputsBy(new FilterBuilder().includePackage(packageToScan))
        .setUrls(ClasspathHelper.forPackage(packageToScan))
        .setScanners(
          new TypeAnnotationsScanner(),
          new MethodParameterScanner(),
          new MethodAnnotationsScanner(),
          new FieldAnnotationsScanner());
    this.config = config;
    this.reflections = new Reflections(configBuilder);
    this.boundAnnotations = new HashSet<Config>();
  }

  /**
   * Scans the specified packages for annotated classes, and applies Config values to them.
   * 
   * @param config the Config to derive values from
   * @return The constructed TypesafeConfigModule.
   */
  public OllieConfigurationModule(OllieServerBuilder config) {
    this(
      config.packageToScan(),
      new ConfigurationProcessor(config.name(), new EnvironmentSelector().select(), null, config.secrets()).process());
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  protected void configure() {
    Set<Constructor> annotatedConstructors = reflections.getConstructorsWithAnyParamAnnotated(Config.class);
    for (Constructor c : annotatedConstructors) {
      Parameter[] params = c.getParameters();
      bindParameters(params);
    }

    Set<Method> annotatedMethods = reflections.getMethodsWithAnyParamAnnotated(Config.class);
    for (Method m : annotatedMethods) {
      Parameter[] params = m.getParameters();
      bindParameters(params);
    }

    Set<Field> annotatedFields = reflections.getFieldsAnnotatedWith(Config.class);
    for (Field f : annotatedFields) {
      Config annotation = f.getAnnotation(Config.class);
      bindValue(f.getType(), f.getAnnotatedType().getType(), annotation, isNullable(f.getAnnotations()));
    }
  }

  private void bindParameters(Parameter[] params) {
    for (Parameter p : params) {
      if (p.isAnnotationPresent(Config.class)) {
        Config annotation = p.getAnnotation(Config.class);
        bindValue(p.getType(), p.getAnnotatedType().getType(), annotation, isNullable(p.getAnnotations()));
      }
    }
  }

  private void bindValue(Class<?> paramClass, Type paramType, Config annotation, boolean nullable) {
    // Prevents multiple bindings on the same annotation
    if (!boundAnnotations.contains(annotation)) {
      @SuppressWarnings("unchecked")
      Key<Object> key = (Key<Object>) Key.get(paramType, annotation);
      String configPath = annotation.value();
      Object configValue = getConfigValue(paramClass, paramType, configPath, nullable);
      if (configValue == null) {
        if (nullable) {
          bind(key).toProvider(NULL_PROVIDER);
        } else {
          throw new ConfigException.Missing(configPath);
        }
      } else {
        bind(key).toInstance(configValue);
      }
      boundAnnotations.add(annotation);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object getConfigValue(Class<?> paramClass, Type paramType, String path, boolean nullable) {
    Optional<Object> extractedValue = ConfigExtractors.extractConfigValue(config, paramClass, path);
    if (extractedValue.isPresent()) {
      return extractedValue.get();
    }

    if (nullable && !config.hasPath(path)) {
      return null;
    }

    ConfigValue configValue = config.getValue(path);
    ConfigValueType valueType = configValue.valueType();
    if (valueType.equals(ConfigValueType.OBJECT) && Map.class.isAssignableFrom(paramClass)) {
      ConfigObject object = config.getObject(path);
      return object.unwrapped();
    } else if (valueType.equals(ConfigValueType.OBJECT)) {
      Object bean = ConfigBeanFactory.create(config.getConfig(path), paramClass);
      return bean;
    } else if (valueType.equals(ConfigValueType.LIST) && List.class.isAssignableFrom(paramClass)) {
      Type listType = ((ParameterizedType) paramType).getActualTypeArguments()[0];

      Optional<List<?>> extractedListValue =
        ListExtractors.extractConfigListValue(config, listType, path);

      if (extractedListValue.isPresent()) {
        return extractedListValue.get();
      } else {
        List<? extends com.typesafe.config.Config> configList = config.getConfigList(path);
        return configList.stream()
          .map(cfg -> {
            Object created = ConfigBeanFactory.create(cfg, (Class) listType);
            return created;
          })
          .collect(Collectors.toList());
      }
    }

    throw new RuntimeException("Cannot obtain config value for " + paramType + " at path: " + path);
  }

  private static boolean isNullable(Annotation[] annotations) {
    if (annotations == null || annotations.length == 0) {
      return false;
    }

    for (Annotation a : annotations) {
      // support any @Nullable - sisu, intellij, etc
      if ("Nullable".equals(a.annotationType().getSimpleName())) {
        return true;
      }
    }

    return false;
  }
}
