package io.avaje.jsonb.spi;

/**
 * Factory that is service loaded to create the adapter for underlying json parsing and generation.
 */
public interface AdapterFactory extends JsonbExtension  {

  /**
   * Create the adapter to use for the underlying json parsing and generation.
   *
   * @param serializeNulls The default setting for serializing nulls
   * @param serializeEmpty The default setting for serializing empty arrays
   * @param failOnUnknown  The default setting for fail when deserializing unknown properties
   * @return The adapter to use
   */
  JsonStreamAdapter create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown);
}
