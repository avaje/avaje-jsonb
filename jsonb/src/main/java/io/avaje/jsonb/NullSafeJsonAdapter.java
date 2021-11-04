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

final class NullSafeJsonAdapter<T> extends JsonAdapter<T> {

  private final JsonAdapter<T> delegate;

  NullSafeJsonAdapter(JsonAdapter<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void toJson(JsonWriter writer, T value) throws IOException {
    if (value == null) {
      writer.nullValue();
    } else {
      delegate.toJson(writer, value);
    }
  }

  @Override
  public T fromJson(JsonReader reader) throws IOException {
    if (reader.peekIsNull()) {
      return reader.nextNull();
    } else {
      return delegate.fromJson(reader);
    }
  }

  @Override
  public String toString() {
    return delegate + ".nullSafe()";
  }
}
