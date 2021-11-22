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

import io.avaje.jsonb.spi.ViewBuilderAware;

import java.lang.reflect.Type;

/**
 * The core API for serialization to and from json.
 */
public abstract class JsonAdapter<T> {

  /**
   * Write the value to the writer.
   */
  public abstract void toJson(JsonWriter writer, T value);

  /**
   * Read the type from the reader.
   */
  public abstract T fromJson(JsonReader reader);

  /**
   * Return a null safe version of this adapter.
   */
  public final JsonAdapter<T> nullSafe() {
    if (this instanceof NullSafeAdapter) {
      return this;
    }
    return new NullSafeAdapter<>(this);
  }

  /**
   * Return true if this adapter represents a json object or json array of objects that supports json views.
   */
  public boolean isViewBuilderAware() {
    return false;
  }

  /**
   * Return the ViewBuilder.Aware for this adapter.
   */
  public ViewBuilderAware viewBuild() {
    throw new IllegalStateException("This adapter is not ViewBuilderAware");
  }

  /**
   * Factory for creating a JsonAdapter.
   */
  public interface Factory {

    /**
     * Create and return a JsonAdapter given the type and annotations or return null.
     * <p>
     * Returning null means that the adapter could be created by another factory.
     */
    JsonAdapter<?> create(Type type, Jsonb jsonb);
  }
}
