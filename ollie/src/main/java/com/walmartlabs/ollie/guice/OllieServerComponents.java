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

import com.walmartlabs.ollie.lifecycle.Lifecycle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

// This class serves as a place to inject server components. For reasons I've yet to determine, I can't inject these components
// directly into the OllieServer using Injector.getClass(OllieServer.class)

@Named
@Singleton
public class OllieServerComponents {

  private final List<Lifecycle> tasks;

  @Inject
  public OllieServerComponents(List<Lifecycle> tasks) {
    this.tasks = tasks;
  }

  public List<Lifecycle> tasks() {
    return tasks;
  }
}
