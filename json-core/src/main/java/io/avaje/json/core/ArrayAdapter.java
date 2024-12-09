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
package io.avaje.json.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts arrays to JSON arrays containing their converted contents.
 * This supports both primitive and object arrays.
 */
final class ArrayAdapter implements JsonAdapter<Object> {

  static JsonAdapter<Object> create(Class<?> elementClass, JsonAdapter<Object> elementAdapter) {
    return new ArrayAdapter(elementClass, elementAdapter).nullSafe();
  }

  static JsonAdapter<byte[]> byteArray() {
    return new ByteArray();
  }

  private final Class<?> elementClass;
  private final JsonAdapter<Object> elementAdapter;

  ArrayAdapter(Class<?> elementClass, JsonAdapter<Object> elementAdapter) {
    this.elementClass = elementClass;
    this.elementAdapter = elementAdapter;
  }

  @Override
  public Object fromJson(JsonReader reader) {
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
  public void toJson(JsonWriter writer, Object value) {
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

  private static final class ByteArray implements JsonAdapter<byte[]> {
    @Override
    public byte[] fromJson(JsonReader reader) {
      return reader.readBinary();
    }

    @Override
    public void toJson(JsonWriter writer, byte[] value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(byte[])";
    }
  }
}
