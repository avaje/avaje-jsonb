package io.avaje.jsonb;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

public abstract class JsonAdapter<T> {

  /**
   * Plan to use this to support "partial objects / views".
   */
  public void toJsonFrom(JsonWriter writer, Supplier<T> supplier) throws IOException {
    toJson(writer, supplier.get());
  }

  /**
   * Write the value to the writer.
   */
  public abstract void toJson(JsonWriter writer, T value) throws IOException;

  /**
   * Read the type from the reader.
   */
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

    /**
     * Create and return a JsonAdapter given the type and annotations or return null.
     * <p>
     * Returning null means that the adapter could be created by another factory.
     */
    JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Jsonb jsonb);
  }
}
