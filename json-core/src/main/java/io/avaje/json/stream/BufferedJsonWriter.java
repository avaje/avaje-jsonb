package io.avaje.json.stream;

import io.avaje.json.JsonWriter;

/**
 * Provides a JsonWriter optimised for returning json as string content.
 */
public interface BufferedJsonWriter extends JsonWriter {

  /**
   * Return result as json string content.
   */
  String result();
}
