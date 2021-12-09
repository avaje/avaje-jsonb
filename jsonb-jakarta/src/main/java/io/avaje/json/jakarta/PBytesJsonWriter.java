package io.avaje.json.jakarta;

import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BytesJsonWriter;
import io.avaje.jsonb.spi.DelegateJsonWriter;

import java.io.ByteArrayOutputStream;

final class PBytesJsonWriter extends DelegateJsonWriter implements BytesJsonWriter {

  private final ByteArrayOutputStream buffer;

  PBytesJsonWriter(JsonWriter delegate, ByteArrayOutputStream buffer) {
    super(delegate);
    this.buffer = buffer;
  }

  @Override
  public byte[] result() {
    delegate.close();
    // final byte[] result = buffer.toByteArray();
    // buffer.release();
    return buffer.toByteArray();
  }
}
