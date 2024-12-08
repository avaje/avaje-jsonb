package io.avaje.jsonb.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.io.SegmentedStringWriter;

import io.avaje.json.JsonException;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.BufferedJsonWriter;
import io.avaje.json.stream.DelegateJsonWriter;

final class JacksonWriteBuffer extends DelegateJsonWriter implements BufferedJsonWriter {

  private final SegmentedStringWriter buffer;

  JacksonWriteBuffer(JsonWriter delegate, SegmentedStringWriter buffer) {
    super(delegate);
    this.buffer = buffer;
  }

  @Override
  public String result() {
    delegate.close();

    try {

      return buffer.getAndClear();

    } catch (IOException io) {

      throw new JsonException(io);
    }
  }
}
