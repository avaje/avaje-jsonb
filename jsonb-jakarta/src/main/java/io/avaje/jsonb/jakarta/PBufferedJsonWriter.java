package io.avaje.jsonb.jakarta;

import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BufferedJsonWriter;
import io.avaje.jsonb.spi.DelegateJsonWriter;

import java.io.StringWriter;

final class PBufferedJsonWriter extends DelegateJsonWriter implements BufferedJsonWriter {

  private final StringWriter buffer;

  PBufferedJsonWriter(JsonWriter delegate, StringWriter buffer) {
    super(delegate);
    this.buffer = buffer;
  }

  @Override
  public String result() {
    delegate.close();
    return buffer.toString();//getAndClear();
  }
}
