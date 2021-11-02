package io.avaje.jsonb.core;

import io.avaje.jsonb.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

final class DJsonType<T> implements JsonType<T> {

  private final DJsonb jsonb;
  private final JsonAdapter<T> adapter;
  private final Type key;
  private JsonType<List<T>> listType;

  DJsonType(DJsonb jsonb, Type key, JsonAdapter<T> adapter) {
    this.jsonb = jsonb;
    this.key = key;
    this.adapter = adapter;
  }

  @Override
  public JsonType<List<T>> list() {
    synchronized (this) {
      if (listType == null) {
        listType = jsonb.listOf(key, adapter);
      }
      return listType;
    }
  }

  @Override
  public String toJson(T bean) throws IOException {
    StringWriter writer = new StringWriter(200);
    try (JsonWriter jsonWriter = jsonb.writer(writer)) {
      toJson(jsonWriter, bean);
    }
    return writer.toString();
  }

  @Override
  public void toJson(JsonWriter writer, T bean) throws IOException {
    adapter.toJson(writer, bean);
  }

  @Override
  public void toJson(Writer writer, T bean) throws IOException {
    adapter.toJson(jsonb.writer(writer), bean);
  }

  @Override
  public void toJson(OutputStream outputStream, T bean) throws IOException {
    adapter.toJson(jsonb.writer(outputStream), bean);
  }

  @Override
  public T fromJson(JsonReader reader) throws IOException {
    return adapter.fromJson(reader);
  }

  @Override
  public T fromJson(String content) throws IOException {
    return adapter.fromJson(jsonb.reader(content));
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
