package io.avaje.jsonb.inject;

import java.lang.reflect.Type;

import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.InjectPlugin;
import io.avaje.json.stream.BufferRecycleStrategy;
import io.avaje.jsonb.Jsonb;

/**
 * Plugin for avaje inject that provides a default Jsonb instance.
 */
public final class DefaultJsonbProvider implements InjectPlugin {

  @Override
  public Type[] provides() {
    return new Type[]{Jsonb.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(null, Jsonb.class, () -> {
      var props = builder.configPlugin();

      return Jsonb.builder()
        .failOnUnknown(props.equalTo("jsonb.deserialize.failOnUnknown", "true"))
        .failOnNullPrimitives(props.equalTo("jsonb.deserialize.failOnNullPrimitives", "true"))
        .mathTypesAsString(props.equalTo("jsonb.serialize.mathTypesAsString", "true"))
        .serializeEmpty(props.notEqualTo("jsonb.serialize.empty", "false"))
        .serializeNulls(props.equalTo("jsonb.serialize.nulls", "true"))
        .bufferRecycling(
          props
            .get("jsonb.bufferRecycling")
            .map(BufferRecycleStrategy::valueOf)
            .orElse(BufferRecycleStrategy.HYBRID_POOL))
        .build();
    });
  }
}
