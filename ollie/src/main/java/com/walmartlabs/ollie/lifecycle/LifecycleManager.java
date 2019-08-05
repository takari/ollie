package com.walmartlabs.ollie.lifecycle;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.walmartlabs.ollie.OllieServerBuilder;
import com.walmartlabs.ollie.guice.OllieShutdownManager;

public class LifecycleManager {

  private final OllieServerBuilder builder;
  private final Injector injector;

  public LifecycleManager(Module module, OllieServerBuilder builder, LifecycleRepository taskRepository, OllieShutdownManager shutdownManager) {
    this.builder = builder;
    this.injector = Guice.createInjector(enableLifeCycleManagement(taskRepository, module));
    Runtime.getRuntime().addShutdownHook(new Thread(shutdownManager::shutdown));  }

  public Injector injector() {
    return injector;
  }

  private static Module enableLifeCycleManagement(LifecycleRepository repository, Module module) {
    return new LifecycleAwareModule(repository, module);
  }
}
