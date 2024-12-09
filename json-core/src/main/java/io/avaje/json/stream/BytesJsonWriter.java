package io.avaje.json.stream;

import io.avaje.json.JsonWriter;

/**
 * JsonWriter optimised for returning json as byte array.
 */
public interface BytesJsonWriter extends JsonWriter {

  /**
   * Return the json result as byte array.
   */
  byte[] result();
}
