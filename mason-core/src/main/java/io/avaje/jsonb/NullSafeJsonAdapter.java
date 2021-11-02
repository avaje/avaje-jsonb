package io.avaje.jsonb;

import java.io.IOException;

final class NullSafeJsonAdapter<T> extends JsonAdapter<T> {

  private final JsonAdapter<T> delegate;

  NullSafeJsonAdapter(JsonAdapter<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void toJson(JsonWriter writer, T value) throws IOException {
    if (value == null) {
      writer.nullValue();
    } else {
      delegate.toJson(writer, value);
    }
  }

  @Override
  public T fromJson(JsonReader reader) throws IOException {
    if (reader.peekIsNull()) {
      return reader.nextNull();
    } else {
      return delegate.fromJson(reader);
    }
  }
}
