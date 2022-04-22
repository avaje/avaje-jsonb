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
import java.util.stream.Stream;

class DJsonType<T> implements JsonType<T> {

  protected final DJsonb jsonb;
  protected final Type type;
  protected final JsonAdapter<T> adapter;

  DJsonType(DJsonb jsonb, Type type, JsonAdapter<T> adapter) {
    this.jsonb = jsonb;
    this.type = type;
    this.adapter = adapter;
  }

  @Override
  public final JsonView<T> view(String dsl) {
    return jsonb.buildView(dsl, adapter, type);
  }

  @Override
  public JsonType<Stream<T>> stream() {
    return new DJsonStreamType<>(jsonb, Types.newParameterizedType(Stream.class, type), new StreamAdapter<>(adapter));
  }

  @Override
  public final JsonType<List<T>> list() {
    return jsonb.type(Types.listOf(type));
  }

  @Override
  public final JsonType<Set<T>> set() {
    return jsonb.type(Types.setOf(type));
  }

  @Override
  public final JsonType<Map<String, T>> map() {
    return jsonb.type(Types.mapOf(type));
  }

  @Override
  public final String toJson(T value) {
    try (BufferedJsonWriter writer = jsonb.bufferedWriter()) {
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public final String toJsonPretty(T value) {
    try (BufferedJsonWriter writer = jsonb.bufferedWriter()) {
      writer.pretty(true);
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public final byte[] toJsonBytes(T value) {
    try (BytesJsonWriter writer = jsonb.bufferedWriterAsBytes()) {
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public final void toJson(T value, JsonWriter writer) {
    adapter.toJson(writer, value);
  }

  @Override
  public final void toJson(T value, Writer writer) {
    try (JsonWriter jsonWriter = jsonb.writer(writer)) {
      adapter.toJson(jsonWriter, value);
    }
  }

  @Override
  public final void toJson(T value, OutputStream outputStream) {
    try (JsonWriter writer = jsonb.writer(outputStream)) {
      adapter.toJson(writer, value);
    }
  }

  @Override
  public final Stream<T> stream(JsonReader reader) {
    return new StreamAdapter<>(adapter).fromJson(reader);
  }

  @Override
  public final T fromObject(Object value) {
    try (JsonReader reader = jsonb.objectReader(value)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public final T fromJson(JsonReader reader) {
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
  public T fromJson(Reader content) {
    try (JsonReader reader = jsonb.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(InputStream inputStream) {
    try (JsonReader reader = jsonb.reader(inputStream)) {
      return adapter.fromJson(reader);
    }
  }
}
