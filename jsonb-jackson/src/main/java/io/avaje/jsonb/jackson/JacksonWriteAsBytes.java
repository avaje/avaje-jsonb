package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.spi.BytesJsonWriter;
import io.avaje.jsonb.spi.DelegateJsonWriter;

final class JacksonWriteAsBytes extends DelegateJsonWriter implements BytesJsonWriter {

  private final ByteArrayBuilder buffer;

  JacksonWriteAsBytes(JsonWriter delegate, ByteArrayBuilder buffer) {
    super(delegate);
    this.buffer = buffer;
  }

  @Override
  public byte[] result() {
    delegate.close();
    final byte[] result = buffer.toByteArray();
    buffer.release();
    return result;
  }
}
