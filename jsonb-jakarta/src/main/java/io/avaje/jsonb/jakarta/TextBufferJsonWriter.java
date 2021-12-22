package io.avaje.jsonb.jakarta;

import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.DelegateJsonWriter;

final class TextBufferJsonWriter extends DelegateJsonWriter implements BufferedJsonWriter {

  private final TextBufferWriter buffer;
  private boolean closed;

  TextBufferJsonWriter(JsonWriter delegate, TextBufferWriter buffer) {
    super(delegate);
    this.buffer = buffer;
  }

  @Override
  public String result() {
    delegate.close();
    closed = true;
    return buffer.getAndClear();
  }

  @Override
  public void close() {
    if (!closed) {
      super.close();
    }
  }
}
