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
package io.avaje.jsonb.core;

import io.avaje.jsonb.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Objects.requireNonNull;

final class BasicTypesAdapters {

  public static final JsonAdapter.Factory FACTORY = new JsonAdapter.Factory() {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Jsonb jsonb) {
      if (!annotations.isEmpty()) return null;
      if (type == Boolean.TYPE) return BOOLEAN_JSON_ADAPTER;
      if (type == Byte.TYPE) return BYTE_JSON_ADAPTER;
      if (type == Character.TYPE) return CHARACTER_JSON_ADAPTER;
      if (type == Double.TYPE) return DOUBLE_JSON_ADAPTER;
      if (type == Float.TYPE) return FLOAT_JSON_ADAPTER;
      if (type == Integer.TYPE) return INTEGER_JSON_ADAPTER;
      if (type == Long.TYPE) return LONG_JSON_ADAPTER;
      if (type == Short.TYPE) return SHORT_JSON_ADAPTER;
      if (type == Boolean.class) return BOOLEAN_JSON_ADAPTER.nullSafe();
      if (type == Byte.class) return BYTE_JSON_ADAPTER.nullSafe();
      if (type == Character.class) return CHARACTER_JSON_ADAPTER.nullSafe();
      if (type == Double.class) return DOUBLE_JSON_ADAPTER.nullSafe();
      if (type == Float.class) return FLOAT_JSON_ADAPTER.nullSafe();
      if (type == Integer.class) return INTEGER_JSON_ADAPTER.nullSafe();
      if (type == Long.class) return LONG_JSON_ADAPTER.nullSafe();
      if (type == Short.class) return SHORT_JSON_ADAPTER.nullSafe();
      if (type == String.class) return STRING_JSON_ADAPTER.nullSafe();
      if (type == Object.class) return new ObjectJsonAdapter(jsonb).nullSafe();

      Class<?> rawType = Util.rawType(type);
      if (rawType.isEnum()) {
        return new EnumJsonAdapter(rawType).nullSafe();
      }
      return null;
    }
  };

  static final JsonAdapter<Boolean> BOOLEAN_JSON_ADAPTER = new JsonAdapter<Boolean>() {
    @Override
    public Boolean fromJson(JsonReader reader) throws IOException {
      return reader.nextBoolean();
    }

    @Override
    public void toJson(JsonWriter writer, Boolean value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Boolean)";
    }
  };

  static final JsonAdapter<Byte> BYTE_JSON_ADAPTER = new JsonAdapter<Byte>() {
    @Override
    public Byte fromJson(JsonReader reader) throws IOException {
      return (byte) rangeCheckNextInt(reader, "a byte", -128, 255);
    }

    @Override
    public void toJson(JsonWriter writer, Byte value) throws IOException {
      writer.value((long) (value.intValue() & 255));
    }

    @Override
    public String toString() {
      return "JsonAdapter(Byte)";
    }
  };

  static final JsonAdapter<Character> CHARACTER_JSON_ADAPTER = new JsonAdapter<Character>() {
    @Override
    public Character fromJson(JsonReader reader) throws IOException {
      String value = reader.nextString();
      if (value.length() > 1) {
        throw new JsonDataException(String.format("Expected %s but was %s at path %s", "a char", '"' + value + '"', reader.path()));
      } else {
        return value.charAt(0);
      }
    }

    @Override
    public void toJson(JsonWriter writer, Character value) throws IOException {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Character)";
    }
  };

  static final JsonAdapter<Double> DOUBLE_JSON_ADAPTER = new JsonAdapter<Double>() {
    @Override
    public Double fromJson(JsonReader reader) throws IOException {
      return reader.nextDouble();
    }

    @Override
    public void toJson(JsonWriter writer, Double value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Double)";
    }
  };

  static final JsonAdapter<Float> FLOAT_JSON_ADAPTER = new JsonAdapter<Float>() {
    @Override
    public Float fromJson(JsonReader reader) throws IOException {
      float value = (float) reader.nextDouble();
      if (Float.isInfinite(value)) { // !reader.isLenient() &&
        throw new JsonDataException("JSON forbids NaN and infinities: " + value + " at path " + reader.path());
      } else {
        return value;
      }
    }

    @Override
    public void toJson(JsonWriter writer, Float value) throws IOException {
      requireNonNull(value);
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Float)";
    }
  };

  static final JsonAdapter<Integer> INTEGER_JSON_ADAPTER = new JsonAdapter<Integer>() {
    @Override
    public Integer fromJson(JsonReader reader) throws IOException {
      return reader.nextInt();
    }

    @Override
    public void toJson(JsonWriter writer, Integer value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Integer)";
    }
  };

  static final JsonAdapter<Long> LONG_JSON_ADAPTER = new JsonAdapter<Long>() {
    @Override
    public Long fromJson(JsonReader reader) throws IOException {
      return reader.nextLong();
    }

    @Override
    public void toJson(JsonWriter writer, Long value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Long)";
    }
  };

  static final JsonAdapter<Short> SHORT_JSON_ADAPTER = new JsonAdapter<Short>() {
    @Override
    public Short fromJson(JsonReader reader) throws IOException {
      return (short) rangeCheckNextInt(reader, "a short", -32768, 32767);
    }

    @Override
    public void toJson(JsonWriter writer, Short value) throws IOException {
      writer.value((long) value.intValue());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Short)";
    }
  };

  static final JsonAdapter<String> STRING_JSON_ADAPTER = new JsonAdapter<String>() {
    @Override
    public String fromJson(JsonReader reader) throws IOException {
      return reader.nextString();
    }

    @Override
    public void toJson(JsonWriter writer, String value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(String)";
    }
  };

  static int rangeCheckNextInt(JsonReader reader, String typeMessage, int min, int max) throws IOException {
    int value = reader.nextInt();
    if (value >= min && value <= max) {
      return value;
    } else {
      throw new JsonDataException(String.format("Expected %s but was %s at path %s", typeMessage, value, reader.path()));
    }
  }

  @SuppressWarnings("rawtypes")
  static final class ObjectJsonAdapter extends JsonAdapter<Object> {
    private final Jsonb jsonb;
    private final JsonAdapter<List> listJsonAdapter;
    private final JsonAdapter<Map> mapAdapter;
    private final JsonAdapter<String> stringAdapter;
    private final JsonAdapter<Double> doubleAdapter;
    private final JsonAdapter<Boolean> booleanAdapter;

    ObjectJsonAdapter(Jsonb jsonb) {
      this.jsonb = jsonb;
      this.listJsonAdapter = jsonb.adapter(List.class);
      this.mapAdapter = jsonb.adapter(Map.class);
      this.stringAdapter = jsonb.adapter(String.class);
      this.doubleAdapter = jsonb.adapter(Double.class);
      this.booleanAdapter = jsonb.adapter(Boolean.class);
    }

    @Override
    public Object fromJson(JsonReader reader) throws IOException {
      switch (reader.peek()) {
        case BEGIN_ARRAY:
          return this.listJsonAdapter.fromJson(reader);
        case BEGIN_OBJECT:
          return this.mapAdapter.fromJson(reader);
        case STRING:
          return this.stringAdapter.fromJson(reader);
        case NUMBER:
          return this.doubleAdapter.fromJson(reader);
        case BOOLEAN:
          return this.booleanAdapter.fromJson(reader);
        case NULL:
          return reader.nextNull();
        default:
          throw new IllegalStateException("Expected a value but was " + reader.peek() + " at path " + reader.path());
      }
    }

    @Override
    public void toJson(JsonWriter writer, Object value) throws IOException {
      Class<?> valueClass = value.getClass();
      if (valueClass == Object.class) {
        writer.beginObject();
        writer.endObject();
      } else {
        this.jsonb.adapter(this.toJsonType(valueClass), Collections.emptySet()).toJson(writer, value);
      }
    }

    private Class<?> toJsonType(Class<?> valueClass) {
      if (Map.class.isAssignableFrom(valueClass)) {
        return Map.class;
      } else {
        return Collection.class.isAssignableFrom(valueClass) ? Collection.class : valueClass;
      }
    }

    public String toString() {
      return "JsonAdapter(Object)";
    }
  }

  static final class EnumJsonAdapter<T extends Enum<T>> extends JsonAdapter<T> {
    private final Class<T> enumType;

    EnumJsonAdapter(Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    public T fromJson(JsonReader reader) throws IOException {
      String value = reader.nextString();
      return Enum.valueOf(enumType, value);
    }

    @Override
    public void toJson(JsonWriter writer, T value) throws IOException {
      writer.value(value.name());
    }

    @Override
    public String toString() {
      return "JsonAdapter(" + this.enumType.getName() + ")";
    }
  }
}
