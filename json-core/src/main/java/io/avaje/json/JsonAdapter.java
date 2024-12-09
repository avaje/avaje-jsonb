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

import io.avaje.json.view.ViewBuilderAware;

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
   * Return true if this adapter represents a json object or json array of objects that supports json views.
   */
  default boolean isViewBuilderAware() {
    return false;
  }

  /**
   * Return the ViewBuilderAware for this adapter.
   */
  default ViewBuilderAware viewBuild() {
    throw new UnsupportedOperationException();
  }
}
