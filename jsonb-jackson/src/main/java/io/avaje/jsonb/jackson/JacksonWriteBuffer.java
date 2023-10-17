package io.avaje.jsonb.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.io.SegmentedStringWriter;

import io.avaje.jsonb.JsonException;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.DelegateJsonWriter;

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
