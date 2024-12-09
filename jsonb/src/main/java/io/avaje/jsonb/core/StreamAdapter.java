package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

final class StreamAdapter<T> implements DJsonClosable<Stream<T>>, JsonAdapter<Stream<T>> {

  private final JsonAdapter<T> elementAdapter;

  StreamAdapter(JsonAdapter<T> elementAdapter) {
    this.elementAdapter = elementAdapter;
  }

  @Override
  public void toJson(JsonWriter writer, Stream<T> value) {
    writer.beginArray();
    value.forEach(bean -> elementAdapter.toJson(writer, bean));
    writer.endArray();
  }

  @Override
  public Stream<T> fromJson(JsonReader reader) {
    Iter<T> iterator = new Iter<>(elementAdapter, reader, false);
    return stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED),false);
  }

  @Override
  public Stream<T> fromJsonWithClose(JsonReader reader) {
    // closing the Stream and consuming all elements closes the JsonReader
    Iter<T> iterator = new Iter<>(elementAdapter, reader, true);
    return stream(spliteratorUnknownSize(iterator, Spliterator.ORDERED),false).onClose(reader::close);
  }

  static class Iter<T> implements Iterator<T> {

    private final JsonAdapter<T> elementAdapter;
    private final JsonReader reader;
    private final boolean closeReader;

    Iter(JsonAdapter<T> elementAdapter, JsonReader reader, boolean closeReader) {
      this.elementAdapter = elementAdapter;
      this.reader = reader;
      this.closeReader = closeReader;
      reader.beginStream();
    }

    @Override
    public boolean hasNext() {
      final boolean result = reader.hasNextStreamElement();
      if (!result) {
        endStream();
      }
      return result;
    }

    @Override
    public T next() {
      return elementAdapter.fromJson(reader);
    }

    private void endStream() {
      reader.endStream();
      if (closeReader) {
        reader.close();
      }
    }
  }
}
