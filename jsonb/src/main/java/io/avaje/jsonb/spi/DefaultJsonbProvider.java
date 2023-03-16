package io.avaje.jsonb.spi;

import io.avaje.inject.BeanScopeBuilder;
import io.avaje.jsonb.Jsonb;

/** Plugin for avaje inject that provides a default Jsonb instance. */
public final class DefaultJsonbProvider implements io.avaje.inject.spi.Plugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[] {Jsonb.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(
        null,
        Jsonb.class,
        () -> {
          var props = builder.propertyPlugin();

          return Jsonb.builder()
              .failOnUnknown(props.equalTo("jsonb.deserialize.failOnUnknown", "true"))
              .mathTypesAsString(props.equalTo("jsonb.serialize.mathTypesAsString", "true"))
              .serializeEmpty(props.notEqualTo("jsonb.serialize.empty", "false"))
              .serializeNulls(props.equalTo("jsonb.serialize.nulls", "true"))
              .build();
        });
  }
}
