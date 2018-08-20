package com.walmartlabs.ollie.toml;

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