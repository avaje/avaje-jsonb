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
package io.avaje.mason.base;

import io.avaje.mason.JsonAdapter;
import io.avaje.mason.JsonReader;
import io.avaje.mason.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts arrays to JSON arrays containing their converted contents. This supports both primitive
 * and object arrays.
 */
final class BaseArrayAdapter extends JsonAdapter<Object> {
  public static final Factory FACTORY =
    (type, annotations, moshi) -> {
      Type elementType = UtilTypes.arrayComponentType(type);
      if (elementType == null) return null;
      if (!annotations.isEmpty()) return null;
      Class<?> elementClass = UtilTypes.getRawType(elementType);
      JsonAdapter<Object> elementAdapter = moshi.adapter(elementType);
      return new BaseArrayAdapter(elementClass, elementAdapter).nullSafe();
    };

  private final Class<?> elementClass;
  private final JsonAdapter<Object> elementAdapter;

  BaseArrayAdapter(Class<?> elementClass, JsonAdapter<Object> elementAdapter) {
    this.elementClass = elementClass;
    this.elementAdapter = elementAdapter;
  }

  @Override
  public Object fromJson(JsonReader reader) throws IOException {
    List<Object> list = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNextElement()) {
      list.add(elementAdapter.fromJson(reader));
    }
    reader.endArray();
    Object array = Array.newInstance(elementClass, list.size());
    for (int i = 0; i < list.size(); i++) {
      Array.set(array, i, list.get(i));
    }
    return array;
  }

  @Override
  public void toJson(JsonWriter writer, Object value) throws IOException {
    writer.beginArray();
    for (int i = 0, size = Array.getLength(value); i < size; i++) {
      elementAdapter.toJson(writer, Array.get(value, i));
    }
    writer.endArray();
  }

  @Override
  public String toString() {
    return elementAdapter + ".array()";
  }
}
