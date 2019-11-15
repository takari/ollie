package com.walmartlabs.ollie.guice;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 - 2019 Takari
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.walmartlabs.ollie.OllieServerBuilder;
import com.walmartlabs.ollie.config.OllieConfigurationModule;
import com.walmartlabs.ollie.database.DatabaseModule;
import com.walmartlabs.ollie.lifecycle.LifecycleManager;
import com.walmartlabs.ollie.lifecycle.LifecycleRepository;
import com.walmartlabs.ollie.util.StringArrayKey;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class InjectorBuilder {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final OllieServerBuilder builder;
  private final ExecutorService executor;
  private final LifecycleRepository taskRepository;
  private final OllieShutdownManager shutdownManager;
  private final ServletContext servletContext;
  private final Map<StringArrayKey, TypeLiteral<? extends HttpServlet>> webServlets = Maps.newLinkedHashMap();
  private final Map<StringArrayKey, TypeLiteral<? extends Filter>> webFilters = Maps.newLinkedHashMap();

  public InjectorBuilder(
      OllieServerBuilder config,
      ExecutorService executor,
      LifecycleRepository taskRepository,
      ServletContext servletContext,
      OllieShutdownManager shutdownManager) {
    this.builder = config;
    this.executor = executor;
    this.taskRepository = taskRepository;
    this.shutdownManager = shutdownManager;
    this.servletContext = servletContext;
  }

  public Injector injector() {
    List<Module> modules = Lists.newArrayList();
    configureModules(modules);
    if (logger.isDebugEnabled() && !modules.isEmpty()) {
      logger.debug("Modules:");
      for (Module module : modules) {
        logger.debug("  {}", module);
      }
    }
    return new LifecycleManager(new WireModule(modules), builder, taskRepository, shutdownManager)
        .injector();
  }

  protected void configureModules(final List<Module> modules) {
    modules.add(
        binder -> {
          binder.bind(OllieServerBuilder.class).toInstance(builder);
          binder.bind(OllieServerComponents.class);
          binder.bind(ExecutorService.class).toInstance(executor);
          binder.bind(ServletContext.class).toInstance(servletContext);
        });
    modules.add(new SpaceModule(new URLClassSpace(getClass().getClassLoader()), BeanScanning.CACHE));
    modules.add(new OllieServletModule(builder, servletContext));
    modules.add(new OllieConfigurationModule(builder));
    modules.add(
        new AbstractModule() {
          @Override
          protected void configure() {
            bindListener(
                Matchers.any(),
                new TypeListener() {
                  @Override
                  public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                    // We have an HttpServlet and it is annotated with @WebServlet
                    if (HttpServlet.class.isAssignableFrom(type.getRawType()) && type.getRawType().getAnnotation(WebServlet.class) != null) {
                      WebServlet webServletAnnotation = type.getRawType().getAnnotation(WebServlet.class);
                      String[] paths = webServletAnnotation.value();
                      logger.info("Registering @WebServlet resource {} on {}.", type.getRawType(), Arrays.toString(paths));
                      // TODO: there has to be a better way to create a TypeLiteral here
                      webServlets.put(new StringArrayKey(paths), (TypeLiteral<? extends HttpServlet>) type);
                    }

                    if (Filter.class.isAssignableFrom(type.getRawType()) && type.getRawType().getAnnotation(WebFilter.class) != null) {
                      WebFilter webFilterAnnotation = type.getRawType().getAnnotation(WebFilter.class);
                      String[] paths = webFilterAnnotation.value();
                      logger.info("Registering @WebFilter resource {} on {}.", type.getRawType(), Arrays.toString(paths));
                      // TODO: there has to be a better way to create a TypeLiteral here
                      webFilters.put(new StringArrayKey(paths), (TypeLiteral<? extends Filter>) type);
                    }
                  }
                });
          }
        });
    if (builder.hasDBSupport()) {
      modules.add(new DatabaseModule(builder));
    }
    modules.addAll(builder.modules());
  }

  public Map<StringArrayKey, TypeLiteral<? extends HttpServlet>> webServlets() {
    return webServlets;
  }

  public Map<StringArrayKey, TypeLiteral<? extends Filter>> webFilters() {
    return webFilters;
  }
}
