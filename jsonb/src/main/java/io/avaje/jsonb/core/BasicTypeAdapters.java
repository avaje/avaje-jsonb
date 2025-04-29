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

import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

final class BasicTypeAdapters {

  @SuppressWarnings({"unchecked", "rawtypes"})
  static final AdapterFactory FACTORY =
      (type, jsonb) -> {
        if (type == Byte.TYPE) return new ByteAdapter();
        if (type == Character.TYPE) return new CharacterAdapter();
        if (type == Short.TYPE) return new ShortAdapter();
        if (type == Byte.class) return new ByteAdapter().nullSafe();
        if (type == Character.class) return new CharacterAdapter().nullSafe();
        if (type == Short.class) return new ShortAdapter().nullSafe();
        if (type == Object.class) return new ObjectJsonAdapter(jsonb).nullSafe();
        if (type == UUID.class) return new UuidAdapter().nullSafe();
        if (type == URL.class) return new UrlAdapter().nullSafe();
        if (type == URI.class) return new UriAdapter().nullSafe();
        if (type == Properties.class) return new PropertiesAdapter(jsonb).nullSafe();
        if (type == InetAddress.class || type == Inet4Address.class || type == Inet6Address.class) {
          return new InetAddressAdapter().nullSafe();
        }
        if (type == StackTraceElement.class) return new StackTraceElementAdapter().nullSafe();
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

  private static final class PropertiesAdapter implements JsonAdapter<Properties> {
    private JsonAdapter<Map<Object, Object>> mapAdapter;

    private PropertiesAdapter(Jsonb jsonb) {
      this.mapAdapter = jsonb.adapter(Types.mapOf(Object.class));
    }

    @Override
    public Properties fromJson(JsonReader reader) {
      var map = mapAdapter.fromJson(reader);
      if (map == null) {
        return null;
      }
      var properties = new Properties();
      properties.putAll(map);
      return properties;
    }

    @Override
    public void toJson(JsonWriter writer, Properties value) {
      mapAdapter.toJson(writer, value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(Properties)";
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

  private static final class CharacterAdapter implements JsonAdapter<Character> {
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

  static int rangeCheckNextInt(JsonReader reader, String typeMessage, int min, int max) {
    int value = reader.readInt();
    if (value >= min && value <= max) {
      return value;
    } else {
      throw new JsonDataException(String.format("Expected %s but was %s at path %s", typeMessage, value, reader.location()));
    }
  }

  @SuppressWarnings("rawtypes")
  private static final class ObjectJsonAdapter implements JsonAdapter<Object> {
    private final Jsonb jsonb;
    private final JsonAdapter<List> listAdapter;
    private final JsonAdapter<Map> mapAdapter;
    private final JsonAdapter<String> stringAdapter;
    private final JsonAdapter<Double> doubleAdapter;
    private final JsonAdapter<Boolean> booleanAdapter;

    ObjectJsonAdapter(Jsonb jsonb) {
      this.jsonb = jsonb;
      this.listAdapter = jsonb.adapter(List.class);
      this.mapAdapter = jsonb.adapter(Map.class);
      this.stringAdapter = jsonb.adapter(String.class);
      this.doubleAdapter = jsonb.adapter(Double.class);
      this.booleanAdapter = jsonb.adapter(Boolean.class);
    }

    @Override
    public Object fromJson(JsonReader reader) {
      switch (reader.currentToken()) {
        case BEGIN_ARRAY:
          return listAdapter.fromJson(reader);
        case BEGIN_OBJECT:
          return mapAdapter.fromJson(reader);
        case STRING:
          return stringAdapter.fromJson(reader);
        case NUMBER:
          var d = doubleAdapter.fromJson(reader);
          if (d % 1 == 0) {
            return d.longValue();
          }
          return d;
        case BOOLEAN:
          return booleanAdapter.fromJson(reader);
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

  private static class EnumJsonAdapter<T extends Enum<T>> implements JsonAdapter<T> {

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

    @Override
    public String toString() {
      return "JsonAdapter(" + this.enumType.getName() + ")";
    }
  }
}
