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
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.walmartlabs.ollie.OllieServerBuilder;
import com.walmartlabs.ollie.database.DatabaseModule;
import com.walmartlabs.ollie.lifecycle.LifecycleManager;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InjectorBuilder {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private final OllieServerBuilder builder;

  public InjectorBuilder(OllieServerBuilder config) {
    this.builder = config;
  }

  public Injector injector() {
    List<Module> modules = Lists.newArrayList();
    configureModules(modules);
    if (log.isDebugEnabled() && !modules.isEmpty()) {
      log.debug("Modules:");
      for (Module module : modules) {
        log.debug("  {}", module);
      }
    }
    return new LifecycleManager(new WireModule(modules), builder).injector();
  }

  // TODO, these all needs to go within the space module
  protected void configureModules(final List<Module> modules) {
    modules.add(new Module() {
      @Override
      public void configure(Binder binder) {
        binder.bind(OllieServerBuilder.class).toInstance(builder);
        binder.bind(OllieServerComponents.class);
      }
    });
    modules.add(new SpaceModule(new URLClassSpace(getClass().getClassLoader()), BeanScanning.CACHE));
    modules.add(new OllieServletModule(builder));
    if (builder.hasDBSupport()) {
      modules.add(new DatabaseModule(builder));
    }
    modules.addAll(builder.modules());
  }
}
