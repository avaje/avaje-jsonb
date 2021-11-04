/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
