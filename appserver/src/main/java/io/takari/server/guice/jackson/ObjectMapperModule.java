package io.takari.server.guice.jackson;

import javax.inject.Named;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.name.Names;

@Named
public class ObjectMapperModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(ObjectMapper.class).annotatedWith(Names.named("siesta")).toProvider(new Provider<ObjectMapper>() {

      private ObjectMapper mapper;

      @Override
      public ObjectMapper get() {
        if (mapper == null) {
          mapper = new ObjectMapper();
          // Pretty print output
          mapper.enable(SerializationFeature.INDENT_OUTPUT);
          // Write dates as ISO-8601
          mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
          // Ignore unknown properties
          mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
          // Ignore nulls
          mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        return mapper;
      }
    });
  }
}
