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

import java.lang.reflect.Type;
import java.net.*;
import java.util.*;

import static java.util.Objects.requireNonNull;

final class BasicTypeAdapters {

  @SuppressWarnings({"unchecked", "rawtypes"})
  static final JsonAdapter.Factory FACTORY =
      (type, jsonb) -> {
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
        if (type == InetAddress.class) return new InetAddressAdapter().nullSafe();
        if (type == Inet4Address.class) return new InetAddressAdapter().nullSafe();
        if (type == Inet6Address.class) return new InetAddressAdapter().nullSafe();
        if (type == StackTraceElement.class) return new StackTraceElementAdapter().nullSafe();
        if (type == Object.class) return new ObjectJsonAdapter(jsonb).nullSafe();
        if (type == Throwable.class) return new ThrowableAdapter(jsonb).nullSafe();

        final Class<?> rawType = Util.rawType(type);
        if (rawType.isEnum()) {
          return new EnumJsonAdapter(rawType).nullSafe();
        }
        return null;
      };

  private static final class UuidAdapter implements JsonAdapter<UUID> {
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

  private static final class UrlAdapter implements JsonAdapter<URL> {
    @Override
    public URL fromJson(JsonReader reader) {
      try {
        return new URI(reader.readString()).toURL();
      } catch (MalformedURLException | URISyntaxException e) {
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

  private static final class UriAdapter implements JsonAdapter<URI> {
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

  private static final class InetAddressAdapter implements JsonAdapter<InetAddress> {
    @Override
    public InetAddress fromJson(JsonReader reader) {
      try {
        return InetAddress.getByName(reader.readString());
      } catch (UnknownHostException e) {
        throw new JsonDataException(e);
      }
    }

    @Override
    public void toJson(JsonWriter writer, InetAddress value) {
      writer.value(value.getHostAddress());
    }

    @Override
    public String toString() {
      return "JsonAdapter(InetAddress)";
    }
  }

  private static final class StackTraceElementAdapter implements JsonAdapter<StackTraceElement> {
    @Override
    public StackTraceElement fromJson(JsonReader reader) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void toJson(JsonWriter writer, StackTraceElement value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(StackTraceElement)";
    }
  }

  private static final class ThrowableAdapter implements JsonAdapter<Throwable> {

    private static final int MAX_STACK = 5;
    private final JsonAdapter<StackTraceElement> stackTraceElementAdapter;

    private ThrowableAdapter(Jsonb jsonb) {
      this.stackTraceElementAdapter = jsonb.adapter(StackTraceElement.class);
    }

    @Override
    public Throwable fromJson(JsonReader reader) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void toJson(JsonWriter writer, Throwable value) {
      writer.beginObject();
      writer.name("type");
      writer.value(value.getClass().toString());
      writer.name("message");
      writer.value(value.getMessage());

      StackTraceElement[] stackTrace = value.getStackTrace();
      if (stackTrace != null && stackTrace.length > 0) {
        int end = Math.min(MAX_STACK, stackTrace.length);
        List<StackTraceElement> stackTraceElements = Arrays.asList(stackTrace).subList(0, end);
        writer.name("stackTrace");
        writer.beginArray();
        for (StackTraceElement element : stackTraceElements) {
          stackTraceElementAdapter.toJson(writer, element);
        }
        writer.endArray();
      }

      final Throwable cause = value.getCause();
      if (cause != null) {
        writer.name("cause");
        toJson(writer, cause);
      }
      final Throwable[] suppressed = value.getSuppressed();
      if (suppressed != null && suppressed.length > 0) {
        writer.name("suppressed");
        writer.beginArray();
        for (Throwable sup : suppressed) {
          toJson(writer, sup);
        }
        writer.endArray();
      }
      writer.endObject();
    }

    @Override
    public String toString() {
      return "JsonAdapter(URI)";
    }
  }

  static final class BooleanAdapter implements JsonAdapter<Boolean> {
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

  static final class ByteAdapter implements JsonAdapter<Byte> {
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

  static final class CharacterAdapter implements JsonAdapter<Character> {
    @Override
    public Character fromJson(JsonReader reader) {
      final String value = reader.readString();
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

  static final class DoubleAdapter implements JsonAdapter<Double> {
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

  static final class FloatAdapter implements JsonAdapter<Float> {
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

  static final class IntegerAdapter implements JsonAdapter<Integer> {
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

  static final class LongAdapter implements JsonAdapter<Long> {
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

  static final class ShortAdapter implements JsonAdapter<Short> {
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

  static final class StringAdapter implements JsonAdapter<String> {
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
  static final class ObjectJsonAdapter implements JsonAdapter<Object> {
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
          var d = this.doubleAdapter.fromJson(reader);
          if (d % 1 == 0) {
            return d.longValue();
          }
          return d;
        case BOOLEAN:
          return this.booleanAdapter.fromJson(reader);
        case NULL:
          return null;
        default:
          throw new IllegalStateException("Expected a value but was " + reader.currentToken() + " at path " + reader.location());
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void toJson(JsonWriter writer, Object value) {
      final Class<?> valueClass = value.getClass();
      if (valueClass == Object.class) {
        writer.beginObject();
        writer.endObject();
      } else if (value instanceof Optional) {
        final var op = (Optional<Object>) value;
        op.ifPresentOrElse(v -> toJson(writer, v), writer::nullValue);
      } else {
        this.jsonb.adapter(toJsonType(valueClass)).toJson(writer, value);
      }
    }

    private Type toJsonType(Class<?> valueClass) {
      if (Map.class.isAssignableFrom(valueClass)) {
        return Map.class;
      } else {
        return Collection.class.isAssignableFrom(valueClass) ? Collection.class : valueClass;
      }
    }

    @Override
    public String toString() {
      return "JsonAdapter(Object)";
    }
  }

  static class EnumJsonAdapter<T extends Enum<T>> implements JsonAdapter<T> {

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
      throw new JsonDataException("Unable to determine enum value " + enumType + " value for " + value + " at " + location);
    }

    @Override
    public String toString() {
      return "JsonAdapter(" + this.enumType.getName() + ")";
    }
  }
}
