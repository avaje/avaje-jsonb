package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.DelegateJsonWriter;

import java.io.IOException;

final class JacksonWriteBuffer extends DelegateJsonWriter implements BufferedJsonWriter {

  private final SegmentedStringWriter buffer;

  JacksonWriteBuffer(JsonWriter delegate, SegmentedStringWriter buffer) {
    super(delegate);
    this.buffer = buffer;
  }

  @Override
  public String result() throws IOException {
    delegate.close();
    return buffer.getAndClear();
  }
}
