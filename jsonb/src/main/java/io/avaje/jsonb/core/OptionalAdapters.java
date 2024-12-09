package io.avaje.jsonb.core;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import io.avaje.json.JsonAdapter;
import io.avaje.json.view.ViewBuilderAware;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

final class OptionalAdapters {

  private OptionalAdapters() {}

  static final AdapterFactory FACTORY = (type, jsonb) -> {
    if (Types.isGenericTypeOf(type, Optional.class)) {
      final Type[] args = Types.typeArguments(type);
      return new OptionalAdapter<>(jsonb, args[0]).nullSafe();
    } else if (type == OptionalInt.class) {
      return new OptionalIntAdapter().nullSafe();
    } else if (type == OptionalDouble.class) {
      return new OptionalDoubleAdapter().nullSafe();
    } else if (type == OptionalLong.class) {
      return new OptionalLongAdapter().nullSafe();
    }
    return null;
  };

  static final class OptionalAdapter<T> implements JsonAdapter<Optional<T>> {

    private final JsonAdapter<T> delegate;

    OptionalAdapter(Jsonb jsonb, Type param0) {
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

  static final class OptionalIntAdapter implements JsonAdapter<OptionalInt> {
    @Override
    public OptionalInt fromJson(JsonReader reader) {
      return OptionalInt.of(reader.readInt());
    }

    @Override
    public void toJson(JsonWriter writer, OptionalInt value) {
      value.ifPresentOrElse(writer::value, writer::nullValue);
    }

    @Override
    public String toString() {
      return "JsonAdapter(OptionalInt)";
    }
  }

  static final class OptionalDoubleAdapter implements JsonAdapter<OptionalDouble> {
    @Override
    public OptionalDouble fromJson(JsonReader reader) {
      return OptionalDouble.of(reader.readDouble());
    }

    @Override
    public void toJson(JsonWriter writer, OptionalDouble value) {
      value.ifPresentOrElse(writer::value, writer::nullValue);
    }

    @Override
    public String toString() {
      return "JsonAdapter(OptionalDouble)";
    }
  }

  static final class OptionalLongAdapter implements JsonAdapter<OptionalLong> {
    @Override
    public OptionalLong fromJson(JsonReader reader) {
      return OptionalLong.of(reader.readLong());
    }

    @Override
    public void toJson(JsonWriter writer, OptionalLong value) {
      value.ifPresentOrElse(writer::value, writer::nullValue);
    }

    @Override
    public String toString() {
      return "JsonAdapter(OptionalLong)";
    }
  }
}
