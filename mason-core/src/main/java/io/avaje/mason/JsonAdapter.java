package io.avaje.mason;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

public abstract class JsonAdapter<T> {

  public void toJsonFrom(JsonWriter writer, Supplier<T> supplier) throws IOException {
    toJson(writer, supplier.get());
  }

  public abstract void toJson(JsonWriter writer, T value) throws IOException;

  public abstract T fromJson(JsonReader reader) throws IOException;

  /**
   * Return a null safe version of this adapter.
   */
  public final JsonAdapter<T> nullSafe() {
    if (this instanceof NullSafeJsonAdapter) {
      return this;
    }
    return new NullSafeJsonAdapter<>(this);
  }

  public interface Factory {

    JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Jsonb jsonb);
  }
}
