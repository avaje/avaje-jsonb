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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

final class BaseAdapters {

  private static final JsonAdapter<String> STRING_ADAPTER = new StringAdapter().nullSafe();
  private static final LongAdapter LONG_ADAPTER = new LongAdapter();
  private static final JsonAdapter<Long> NULL_SAFE_LONG = LONG_ADAPTER.nullSafe();
  private static final IntegerAdapter INTEGER_ADAPTER = new IntegerAdapter();
  private static final JsonAdapter<Integer> NULL_SAFE_INT = INTEGER_ADAPTER.nullSafe();
  private static final FloatAdapter FLOAT_ADAPTER = new FloatAdapter();
  private static final JsonAdapter<Float> NULL_SAFE_FLOAT = FLOAT_ADAPTER.nullSafe();
  private static final DoubleAdapter DOUBLE_ADAPTER = new DoubleAdapter();
  private static final JsonAdapter<Double> NULL_SAFE_DOUBLE = DOUBLE_ADAPTER.nullSafe();
  private static final BooleanAdapter BOOLEAN_ADAPTER = new BooleanAdapter();
  private static final JsonAdapter<Boolean> NULL_SAFE_BOOLEAN = BOOLEAN_ADAPTER.nullSafe();

  static JsonAdapter<?> create(Type type) {
    if (type == Boolean.TYPE) return BOOLEAN_ADAPTER;
    if (type == Double.TYPE) return DOUBLE_ADAPTER;
    if (type == Float.TYPE) return FLOAT_ADAPTER;
    if (type == Integer.TYPE) return INTEGER_ADAPTER;
    if (type == Long.TYPE) return LONG_ADAPTER;
    if (type == Boolean.class) return NULL_SAFE_BOOLEAN;
    if (type == Double.class) return NULL_SAFE_DOUBLE;
    if (type == Float.class) return NULL_SAFE_FLOAT;
    if (type == Integer.class) return NULL_SAFE_INT;
    if (type == Long.class) return NULL_SAFE_LONG;
    if (type == String.class) return STRING_ADAPTER;

    return null;
  }

  private static final class BooleanAdapter implements JsonAdapter<Boolean> {
    @Override
    public Boolean fromJson(JsonReader reader) {
      return reader.readBoolean();
    }

    @Override
    public void toJson(JsonWriter writer, Boolean value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Boolean)";
    }
  }

  private static final class DoubleAdapter implements JsonAdapter<Double> {
    @Override
    public Double fromJson(JsonReader reader) {
      return reader.readDouble();
    }

    @Override
    public void toJson(JsonWriter writer, Double value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Double)";
    }
  }

  private static final class FloatAdapter implements JsonAdapter<Float> {
    @Override
    public Float fromJson(JsonReader reader) {
      float value = (float) reader.readDouble();
      if (Float.isInfinite(value)) { // !reader.isLenient() &&
        throw new JsonDataException("JSON forbids NaN and infinities: " + value + " at path " + reader.location());
      } else {
        return value;
      }
    }

    @Override
    public void toJson(JsonWriter writer, Float value) {
      requireNonNull(value);
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Float)";
    }
  }

  private static final class IntegerAdapter implements JsonAdapter<Integer> {
    @Override
    public Integer fromJson(JsonReader reader) {
      return reader.readInt();
    }

    @Override
    public void toJson(JsonWriter writer, Integer value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Integer)";
    }
  }

  private static final class LongAdapter implements JsonAdapter<Long> {
    @Override
    public Long fromJson(JsonReader reader) {
      return reader.readLong();
    }

    @Override
    public void toJson(JsonWriter writer, Long value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Long)";
    }
  }

  private static final class StringAdapter implements JsonAdapter<String> {
    @Override
    public String fromJson(JsonReader reader) {
      return reader.readString();
    }

    @Override
    public void toJson(JsonWriter writer, String value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(String)";
    }
  }

}
