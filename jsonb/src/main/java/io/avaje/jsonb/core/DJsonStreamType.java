package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.jsonb.JsonType;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.stream.Stream;

final class DJsonStreamType<T> extends DJsonType<T> {

  private final DJsonClosable<T> streamAdapter;

  @SuppressWarnings("unchecked")
  DJsonStreamType(DJsonb jsonb, Type type, JsonAdapter<T> adapter) {
    super(jsonb, type, adapter);
    this.streamAdapter = (DJsonClosable<T>)adapter;
  }

  @Override
  public JsonType<Stream<T>> stream() {
    throw new UnsupportedOperationException("Not allowed streaming type of an underlying streaming type");
  }

  @Override
  public T fromJson(String content) {
    // closing Stream, closes the JsonReader
    JsonReader reader = jsonb.reader(content);
    return streamAdapter.fromJsonWithClose(reader);
  }

  @Override
  public T fromJson(byte[] content) {
    // closing Stream, closes the JsonReader
    JsonReader reader = jsonb.reader(content);
    return streamAdapter.fromJsonWithClose(reader);
  }

  @Override
  public T fromJson(Reader content) {
    // closing Stream, closes the JsonReader
    JsonReader reader = jsonb.reader(content);
    return streamAdapter.fromJsonWithClose(reader);
  }

  @Override
  public T fromJson(InputStream inputStream) {
    // closing Stream, closes the JsonReader
    JsonReader reader = jsonb.reader(inputStream);
    return streamAdapter.fromJsonWithClose(reader);
  }
}
