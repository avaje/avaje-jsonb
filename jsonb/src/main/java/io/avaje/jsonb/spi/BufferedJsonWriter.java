package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonWriter;

/**
 * Provides a JsonWriter optimised for returning json as string content.
 */
public interface BufferedJsonWriter extends JsonWriter {

  /**
   * Return result as json string content.
   */
  String result();
}
