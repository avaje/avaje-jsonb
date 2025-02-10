package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.core.CoreTypes;
import io.avaje.json.mapper.JsonMapper;
import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.BytesJsonWriter;
import io.avaje.json.stream.JsonStream;

import java.io.*;
import java.util.List;
import java.util.Map;

final class DMapper<T> implements JsonMapper.Type<T> {

  private final JsonAdapter<T> adapter;
  private final JsonStream jsonStream;

  DMapper(JsonAdapter<T> adapter, JsonStream jsonStream) {
    this.adapter = adapter;
    this.jsonStream = jsonStream;
  }

  @Override
  public JsonMapper.Type<List<T>> list() {
    final JsonAdapter<List<T>> list = CoreTypes.createList(adapter);
    return new DMapper<>(list, jsonStream);
  }

  @Override
  public JsonMapper.Type<Map<String, T>> map() {
    final JsonAdapter<Map<String, T>> map = CoreTypes.createMap(adapter);
    return new DMapper<>(map, jsonStream);
  }

  @Override
  public T fromJson(JsonReader reader) {
    return adapter.fromJson(reader);
  }

  @Override
  public T fromJson(String content) {
    try (JsonReader reader = jsonStream.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(byte[] content) {
    try (JsonReader reader = jsonStream.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(Reader content) {
    try (JsonReader reader = jsonStream.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public T fromJson(InputStream content) {
    try (JsonReader reader = jsonStream.reader(content)) {
      return adapter.fromJson(reader);
    }
  }

  @Override
  public String toJson(T value) {
    try (BufferedJsonWriter writer = jsonStream.bufferedWriter()) {
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public String toJsonPretty(T value) {
    try (BufferedJsonWriter writer = jsonStream.bufferedWriter()) {
      writer.pretty(true);
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public byte[] toJsonBytes(T value) {
    try (BytesJsonWriter writer = jsonStream.bufferedWriterAsBytes()) {
      toJson(value, writer);
      return writer.result();
    }
  }

  @Override
  public void toJson(T value, JsonWriter writer) {
    try {
      adapter.toJson(writer, value);
    } catch (RuntimeException e) {
      writer.markIncomplete();
      throw new JsonException(e);
    }
  }

  @Override
  public void toJson(T value, Writer writer) {
    try (JsonWriter jsonWriter = jsonStream.writer(writer)) {
      toJson(value, jsonWriter);
    }
  }

  @Override
  public void toJson(T value, OutputStream outputStream) {
    try (JsonWriter writer = jsonStream.writer(outputStream)) {
      toJson(value, writer);
    }
    close(outputStream);
  }

  private void close(Closeable outputStream) {
    try {
      outputStream.close();
    } catch (IOException e) {
      throw new UncheckedIOException("Error closing stream", e);
    }
  }

}
