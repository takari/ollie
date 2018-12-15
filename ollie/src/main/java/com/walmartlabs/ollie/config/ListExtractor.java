package com.walmartlabs.ollie.config;

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

import java.lang.reflect.Type;
import java.util.List;

import com.typesafe.config.Config;

/**
 * Extracts {@link List} values from a particular {@link Config}.
 * 
 * @author jason
 */
public interface ListExtractor {
  /**
   * @param config the {@link Config} to extract from
   * @param path the {@link Config} path
   * @return the extracted list value
   */
  public List<?> extractListValue(Config config, String path);

  /**
   * @return the {@link List} type this {@link ListExtractor} extracts.
   */
  public Type getMatchingParameterizedType();
}


