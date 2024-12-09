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
package io.avaje.json;

/**
 * The core API for serialization to and from json.
 */
public interface JsonAdapter<T> {

  /**
   * Write the value to the writer.
   */
  void toJson(JsonWriter writer, T value);

  /**
   * Read the type from the reader.
   */
  T fromJson(JsonReader reader);

  /**
   * Return a null safe version of this adapter.
   */
  default JsonAdapter<T> nullSafe() {
    if (this instanceof NullSafeAdapter) {
      return this;
    }
    return new NullSafeAdapter<>(this);
  }

  /**
   * Unwrap a ViewBuilderAware trait if implemented or otherwise null.
   * <p>
   * This is implemented on JsonAdapters that also implement ViewBuilderAware
   * which can create a "view" for some select properties of the adapter
   * (rather than including all properties of the adapter).
   * <p>
   * This defaults to returning null meaning that it does not support
   * ViewBuilderAware and must always render all it's properties.
   */
  default <U> U unwrap(Class<U> viewBuilderAwareClass) {
    return null;
  }
}
