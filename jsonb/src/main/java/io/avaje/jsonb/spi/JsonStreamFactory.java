package io.avaje.jsonb.spi;

import io.avaje.json.stream.JsonStream;

/**
 * Factory that is service loaded to create the adapter for underlying json parsing and generation.
 */
public interface JsonStreamFactory extends JsonbExtension  {

  /**
   * Create the adapter to use for the underlying json parsing and generation.
   *
   * @param serializeNulls The default setting for serializing nulls
   * @param serializeEmpty The default setting for serializing empty arrays
   * @param failOnUnknown  The default setting for fail when deserializing unknown properties
   * @return The adapter to use
   */
  JsonStream create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown);
}
