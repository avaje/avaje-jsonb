/*
 * Copyright (C) 2015 Square, Inc.
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
package io.avaje.jsonb.core;

import io.avaje.jsonb.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts maps with string keys to JSON objects.
 */
final class MapAdapter<V> extends JsonAdapter<Map<String, V>> {

  static final Factory FACTORY = (type, jsonb) -> {
    Class<?> rawType = Util.rawType(type);
    if (rawType != Map.class) {
      return null;
    }
    Type valueType = Util.mapValueType(type, rawType);
    return new MapAdapter<>(jsonb, valueType).nullSafe();
  };

  private final JsonAdapter<V> valueAdapter;

  MapAdapter(Jsonb jsonb, Type valueType) {
    this.valueAdapter = jsonb.adapter(valueType);
  }

  @Override
  public void toJson(JsonWriter writer, Map<String, V> map) throws IOException {
    writer.beginObject();
    for (Map.Entry<String, V> entry : map.entrySet()) {
      if (entry.getKey() == null) {
        throw new JsonDataException("Map key is null at " + writer.path());
      }
      writer.name(entry.getKey());
      valueAdapter.toJson(writer, entry.getValue());
    }
    writer.endObject();
  }

  @Override
  public Map<String, V> fromJson(JsonReader reader) throws IOException {
    Map<String, V> result = new LinkedHashMap<>();
    reader.beginObject();
    while (reader.hasNextField()) {
      String name = reader.nextField();
      V value = valueAdapter.fromJson(reader);
      V replaced = result.put(name, value);
      if (replaced != null) {
        throw new JsonDataException(
          "Map key '"
            + name
            + "' has multiple values at path "
            + reader.path()
            + ": "
            + replaced
            + " and "
            + value);
      }
    }
    reader.endObject();
    return result;
  }

  @Override
  public String toString() {
    return "MapAdapter(" + valueAdapter + ")";
  }
}
