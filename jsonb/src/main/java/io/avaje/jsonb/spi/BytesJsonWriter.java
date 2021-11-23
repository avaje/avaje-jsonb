package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonWriter;

/**
 * JsonWriter optimised for returning json as byte array.
 */
public interface BytesJsonWriter extends JsonWriter {

  /**
   * Return the json result as byte array.
   */
  byte[] result();
}
