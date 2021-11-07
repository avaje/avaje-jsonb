package io.avaje.jsonb.core;

import io.avaje.jsonb.*;
import io.avaje.jsonb.spi.BufferedJsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class DJsonType<T> implements JsonType<T> {

  private final DJsonb jsonb;
  private final Type type;
  private final JsonAdapter<T> adapter;

  DJsonType(DJsonb jsonb, Type type, JsonAdapter<T> adapter) {
    this.jsonb = jsonb;
    this.type = type;
    this.adapter = adapter;
  }

  @Override
  public JsonType<List<T>> list() {
    return jsonb.type(Types.listOf(type));
  }

  @Override
  public JsonType<Set<T>> set() {
    return jsonb.type(Types.setOf(type));
  }

  @Override
  public JsonType<Map<String, T>> map() {
    return jsonb.type(Types.mapOf(type));
  }

  @Override
  public String toJson(T value) throws IOException {
//    StringWriter writer = new StringWriter(200);
//    try (JsonWriter jsonWriter = jsonb.writer(writer)) {
//      toJson(jsonWriter, value);
//    }
//    return writer.toString();
    BufferedJsonWriter bufferedJsonWriter = jsonb.bufferedWriter();
    toJson(bufferedJsonWriter, value);
    return bufferedJsonWriter.result();
  }

  @Override
  public void toJson(JsonWriter writer, T value) throws IOException {
    adapter.toJson(writer, value);
  }

  @Override
  public void toJson(Writer writer, T value) throws IOException {
    adapter.toJson(jsonb.writer(writer), value);
  }

  @Override
  public void toJson(OutputStream outputStream, T value) throws IOException {
    adapter.toJson(jsonb.writer(outputStream), value);
  }

  @Override
  public T fromObject(Object value) throws IOException {
    try (JsonReader reader = jsonb.objectReader(value)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(JsonReader reader) throws IOException {
    return adapter.fromJson(reader);
  }

  @Override
  public T fromJson(String content) throws IOException {
    try (JsonReader reader = jsonb.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(Reader reader) throws IOException {
    return adapter.fromJson(jsonb.reader(reader));
  }

  @Override
  public T fromJson(InputStream inputStream) throws IOException {
    return adapter.fromJson(jsonb.reader(inputStream));
  }
}
