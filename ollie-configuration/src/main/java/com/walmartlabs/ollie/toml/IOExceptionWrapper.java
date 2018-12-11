package com.walmartlabs.ollie.toml;

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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

abstract class IOExceptionWrapper {

  private IOExceptionWrapper() {}

  static <T> Consumer<T> consumer(ThrowingConsumer<T> c) {
    return (T t) -> {
      try {
        c.accept(t);
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    };
  }

  static interface ThrowingConsumer<T> {
    void accept(T t) throws IOException;
  }
}
