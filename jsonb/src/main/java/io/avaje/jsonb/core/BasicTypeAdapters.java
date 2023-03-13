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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static java.util.Objects.requireNonNull;

final class BasicTypeAdapters {

  static final JsonAdapter.Factory FACTORY = (type, jsonb) -> {
    if (type == Boolean.TYPE) return new BooleanAdapter();
    if (type == Byte.TYPE) return new ByteAdapter();
    if (type == Character.TYPE) return new CharacterAdapter();
    if (type == Double.TYPE) return new DoubleAdapter();
    if (type == Float.TYPE) return new FloatAdapter();
    if (type == Integer.TYPE) return new IntegerAdapter();
    if (type == Long.TYPE) return new LongAdapter();
    if (type == Short.TYPE) return new ShortAdapter();
    if (type == Boolean.class) return new BooleanAdapter().nullSafe();
    if (type == Byte.class) return new ByteAdapter().nullSafe();
    if (type == Character.class) return new CharacterAdapter().nullSafe();
    if (type == Double.class) return new DoubleAdapter().nullSafe();
    if (type == Float.class) return new FloatAdapter().nullSafe();
    if (type == Integer.class) return new IntegerAdapter().nullSafe();
    if (type == Long.class) return new LongAdapter().nullSafe();
    if (type == Short.class) return new ShortAdapter().nullSafe();
    if (type == String.class) return new StringAdapter().nullSafe();
    if (type == UUID.class) return new UuidAdapter().nullSafe();
    if (type == URL.class) return new UrlAdapter().nullSafe();
    if (type == URI.class) return new UriAdapter().nullSafe();
    if (type == Object.class) return new ObjectJsonAdapter(jsonb).nullSafe();

    Class<?> rawType = Util.rawType(type);
    if (rawType.isEnum()) {
      return createEnumAdapter(rawType);
    }
    return null;
  };

  private static final class UuidAdapter extends JsonAdapter<UUID> {
    @Override
    public UUID fromJson(JsonReader reader) {
      return UUID.fromString(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, UUID value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(UUID)";
    }
  }

  private static final class UrlAdapter extends JsonAdapter<URL> {
    @Override
    public URL fromJson(JsonReader reader) {
      try {
        return new URL(reader.readString());
      } catch (MalformedURLException e) {
        throw new JsonDataException(e);
      }
    }

    @Override
    public void toJson(JsonWriter writer, URL value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(URL)";
    }
  }

  private static final class UriAdapter extends JsonAdapter<URI> {
    @Override
    public URI fromJson(JsonReader reader) {
      return URI.create(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, URI value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(URI)";
    }
  }

  static final class BooleanAdapter extends JsonAdapter<Boolean> {
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

  static final class ByteAdapter extends JsonAdapter<Byte> {
    @Override
    public Byte fromJson(JsonReader reader) {
      return (byte) rangeCheckNextInt(reader, "a byte", -128, 255);
    }

    @Override
    public void toJson(JsonWriter writer, Byte value) {
      writer.value((long) (value.intValue() & 255));
    }

    @Override
    public String toString() {
      return "JsonAdapter(Byte)";
    }
  }

  static final class CharacterAdapter extends JsonAdapter<Character> {
    @Override
    public Character fromJson(JsonReader reader) {
      String value = reader.readString();
      if (value.length() > 1) {
        throw new JsonDataException(String.format("Expected %s but was %s at path %s", "a char", '"' + value + '"', reader.location()));
      } else {
        return value.charAt(0);
      }
    }

    @Override
    public void toJson(JsonWriter writer, Character value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Character)";
    }
  }

  static final class DoubleAdapter extends JsonAdapter<Double> {
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

  static final class FloatAdapter extends JsonAdapter<Float> {
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

  static final class IntegerAdapter extends JsonAdapter<Integer> {
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

  static final class LongAdapter extends JsonAdapter<Long> {
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

  static final class ShortAdapter extends JsonAdapter<Short> {
    @Override
    public Short fromJson(JsonReader reader) {
      return (short) rangeCheckNextInt(reader, "a short", -32768, 32767);
    }

    @Override
    public void toJson(JsonWriter writer, Short value) {
      writer.value((long) value.intValue());
    }

    @Override
    public String toString() {
      return "JsonAdapter(Short)";
    }
  }

  static final class StringAdapter extends JsonAdapter<String> {
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

  static int rangeCheckNextInt(JsonReader reader, String typeMessage, int min, int max) {
    int value = reader.readInt();
    if (value >= min && value <= max) {
      return value;
    } else {
      throw new JsonDataException(String.format("Expected %s but was %s at path %s", typeMessage, value, reader.location()));
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
    public Object fromJson(JsonReader reader) {
      switch (reader.currentToken()) {
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
          return null;
        default:
          throw new IllegalStateException("Expected a value but was " + reader.currentToken() + " at path " + reader.location());
      }
    }

    @Override
    public void toJson(JsonWriter writer, Object value) {
      Class<?> valueClass = value.getClass();
      if (valueClass == Object.class) {
        writer.beginObject();
        writer.endObject();
      } else {
        this.jsonb.adapter(this.toJsonType(valueClass)).toJson(writer, value);
      }
    }

    private Type toJsonType(Class<?> valueClass) {
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

  @SuppressWarnings("rawtypes")
  private static JsonAdapter<?> createEnumAdapter(Class<?> rawType) {
    for (Method declaredMethod : rawType.getDeclaredMethods()) {
      for (Annotation annotation : declaredMethod.getDeclaredAnnotations()) {
        if (isJsonValue(annotation)) {
          return enumMap(rawType, declaredMethod);
        }
      }
    }
    return new EnumJsonAdapter(rawType).nullSafe();
  }

  private static boolean isJsonValue(Annotation annotation) {
    return Json.Value.class == annotation.annotationType()
      || annotation.annotationType().getCanonicalName().endsWith("JsonValue"); // e.g. Jackson annotation
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static JsonAdapter<?> enumMap(Class<?> type, Method method) {
    final Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
    final var returnType = method.getReturnType();
    if (returnType == int.class) {
      return new EnumIntValueMap(method, enumType);
    } else if (returnType == boolean.class) {

      return new EnumBoolValueMap(method, enumType);
    } else if (returnType == long.class) {

      return new EnumLongValueMap(method, enumType);
    } else if (returnType == double.class) {
      return new EnumDoubleValueMap(method, enumType);
    } else {
      return new EnumValueMap(method, enumType);
    }
  }

  static class EnumValueMap<T extends Enum<T>> extends EnumJsonAdapter<T> {

    private final Map<T, String> toValue;
    private final Map<String, T> toEnum = new HashMap<>();

    EnumValueMap(Method method, Class<T> enumType) {
      super(enumType);
      this.toValue = new EnumMap<>(enumType);
      for (final var enumConstant : enumType.getEnumConstants()) {
        try {
          final var val = String.valueOf(method.invoke(enumConstant));
          toValue.put(enumConstant, val);
          toEnum.put(val, enumConstant);
        } catch (final Exception e) {
          throw new JsonException("Error trying to invoke Json.Value method on " + enumConstant, e);
        }
      }
    }

    @Override
    public void toJson(JsonWriter writer, T value) {
      writer.value(toValue.get(value));
    }

    @Override
    public T fromJson(JsonReader reader) {
      final String value = reader.readString();
      final var enumConstant = toEnum.get(value);
      if (enumConstant == null) {
        throwException(value, reader.location());
      }
      return enumConstant;
    }
  }

  static class EnumIntValueMap<T extends Enum<T>> extends EnumJsonAdapter<T> {

    private final Map<T, Integer> toValue;
    private final Map<Integer, T> toEnum = new HashMap<>();

    EnumIntValueMap(Method method, Class<T> enumType) {
      super(enumType);
      this.toValue = new EnumMap<>(enumType);
      for (final var enumConstant : enumType.getEnumConstants()) {
        try {
          final Integer val = (Integer) method.invoke(enumConstant);
          toValue.put(enumConstant, val);
          toEnum.put(val, enumConstant);
        } catch (final Exception e) {
          throw new JsonException("Error trying to invoke Json.Value method on " + enumConstant, e);
        }
      }
    }

    @Override
    public void toJson(JsonWriter writer, T value) {
      writer.value(toValue.get(value));
    }

    @Override
    public T fromJson(JsonReader reader) {
      final int value = reader.readInt();
      final var enumConstant = toEnum.get(value);
      if (enumConstant == null) {
        throwException(value, reader.location());
      }
      return enumConstant;
    }
  }

  static class EnumDoubleValueMap<T extends Enum<T>> extends EnumJsonAdapter<T> {

    private final Map<T, Double> toValue;
    private final Map<Double, T> toEnum = new HashMap<>();

    EnumDoubleValueMap(Method method, Class<T> enumType) {
      super(enumType);
      this.toValue = new EnumMap<>(enumType);
      for (final var enumConstant : enumType.getEnumConstants()) {
        try {
          final Double val = (Double) method.invoke(enumConstant);
          toValue.put(enumConstant, val);
          toEnum.put(val, enumConstant);
        } catch (final Exception e) {
          throw new JsonException("Error trying to invoke Json.Value method on " + enumConstant, e);
        }
      }
    }

    @Override
    public void toJson(JsonWriter writer, T value) {
      writer.value(toValue.get(value));
    }

    @Override
    public T fromJson(JsonReader reader) {
      final var value = reader.readDouble();
      final var enumConstant = toEnum.get(value);
      if (enumConstant == null) {
        throwException(value, reader.location());
      }
      return enumConstant;
    }
  }

  static final class EnumLongValueMap<T extends Enum<T>> extends EnumJsonAdapter<T> {

    private final Map<T, Long> toValue;
    private final Map<Long, T> toEnum = new HashMap<>();

    EnumLongValueMap(Method method, Class<T> enumType) {
      super(enumType);
      this.toValue = new EnumMap<>(enumType);
      for (final var enumConstant : enumType.getEnumConstants()) {
        try {
          final var val = (Long) method.invoke(enumConstant);
          toValue.put(enumConstant, val);
          toEnum.put(val, enumConstant);
        } catch (final Exception e) {
          throw new JsonException("Error trying to invoke Json.Value method on " + enumConstant, e);
        }
      }
    }

    @Override
    public void toJson(JsonWriter writer, T value) {
      writer.value(toValue.get(value));
    }

    @Override
    public T fromJson(JsonReader reader) {
      final var value = reader.readLong();
      final var enumConstant = toEnum.get(value);
      if (enumConstant == null) {
        throwException(value, reader.location());
      }
      return enumConstant;
    }
  }

  static final class EnumBoolValueMap<T extends Enum<T>> extends EnumJsonAdapter<T> {

    private T trueEnum = null;
    private T falseEnum = null;

    EnumBoolValueMap(Method method, Class<T> enumType) {
      super(enumType);
      for (final var enumConstant : enumType.getEnumConstants()) {
        try {
          final boolean val = (boolean) method.invoke(enumConstant);
          if (val) {
            trueEnum = enumConstant;
          } else {
            falseEnum = enumConstant;
          }
        } catch (final Exception e) {
          throw new JsonException("Error trying to invoke Json.Value method on " + enumConstant, e);
        }
      }
    }

    @Override
    public void toJson(JsonWriter writer, T value) {

      if (value == null) {
        writer.value((Boolean) null);
      } else if (value == trueEnum) {
        writer.value(true);
      } else {
        writer.value(false);
      }
    }

    @Override
    public T fromJson(JsonReader reader) {
      final boolean value = reader.readBoolean();
      T enumConstant;

      if (value) {
        enumConstant = trueEnum;
      } else {
        enumConstant = falseEnum;
      }
      return enumConstant;
    }
  }

  static class EnumJsonAdapter<T extends Enum<T>> extends JsonAdapter<T> {

    protected final Class<T> enumType;

    EnumJsonAdapter(Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    public T fromJson(JsonReader reader) {
      final String value = reader.readString();
      return Enum.valueOf(enumType, value);
    }

    @Override
    public void toJson(JsonWriter writer, T value) {
      if (value != null) {
        writer.value(value.name());
      } else {
        writer.value((String) null);
      }
    }

    protected final void throwException(Object value, String location) {
      throw new JsonDataException(
          "Unable to determine enum value " + enumType + " value for " + value + " at " + location);
    }

    @Override
    public String toString() {
      return "JsonAdapter(" + this.enumType.getName() + ")";
    }
  }
}
