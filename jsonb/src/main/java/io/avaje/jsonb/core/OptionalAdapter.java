package io.avaje.jsonb.core;

import java.lang.reflect.Type;
import java.util.Optional;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.avaje.jsonb.spi.ViewBuilderAware;

/** Converts collection types to JSON arrays containing their converted contents. */
class OptionalAdapter<T> implements JsonAdapter<Optional<T>> {

  public static final JsonAdapter.Factory FACTORY =
      (type, jsonb) -> {
        if (Types.isGenericTypeOf(type, Optional.class)) {
          final Type[] args = Types.typeArguments(type);
          return new OptionalAdapter<>(jsonb, args[0]).nullSafe();
        }
        return null;
      };

  private final JsonAdapter<T> delegate;

  public OptionalAdapter(Jsonb jsonb, Type param0) {
    this.delegate = jsonb.adapter(param0);
  }

  @Override
  public void toJson(JsonWriter writer, Optional<T> value) {
    delegate.toJson(writer, value.orElse(null));
  }

  @Override
  public Optional<T> fromJson(JsonReader reader) {

    return Optional.ofNullable(delegate.fromJson(reader));
  }

  @Override
  public boolean isViewBuilderAware() {
    return delegate.isViewBuilderAware();
  }

  @Override
  public ViewBuilderAware viewBuild() {
    return delegate.viewBuild();
  }

  @Override
  public String toString() {
    return delegate + ".optional()";
  }
}
