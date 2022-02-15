package io.avaje.jsonb.core;

import io.avaje.jsonb.*;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.BytesJsonWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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
  public JsonView<T> view(String dsl) {
    return jsonb.buildView(dsl, adapter, type);
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
  public String toJson(T value) {
    try (BufferedJsonWriter writer = jsonb.bufferedWriter()) {
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public byte[] toJsonBytes(T value) {
    try (BytesJsonWriter writer = jsonb.bufferedWriterAsBytes()) {
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public void toJson(T value, JsonWriter writer) {
    adapter.toJson(writer, value);
  }

  @Override
  public void toJson(T value, Writer writer) {
    try (JsonWriter jsonWriter = jsonb.writer(writer)) {
      adapter.toJson(jsonWriter, value);
    }
  }

  @Override
  public void toJson(T value, OutputStream outputStream) {
    try (JsonWriter writer = jsonb.writer(outputStream)) {
      adapter.toJson(writer, value);
    }
  }

  @Override
  public T fromObject(Object value) {
    try (JsonReader reader = jsonb.objectReader(value)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(JsonReader reader) {
    return adapter.fromJson(reader);
  }

  @Override
  public T fromJson(String content) {
    try (JsonReader reader = jsonb.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(byte[] content) {
    try (JsonReader reader = jsonb.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(Reader reader) {
    return adapter.fromJson(jsonb.reader(reader));
  }

  @Override
  public T fromJson(InputStream inputStream) {
    return adapter.fromJson(jsonb.reader(inputStream));
  }
}
