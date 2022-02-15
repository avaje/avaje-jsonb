package io.avaje.jsonb.jackson;

import io.avaje.jsonb.spi.JsonStreamAdapter;
import io.avaje.jsonb.spi.AdapterFactory;

/**
 * Jackson Core based adapter.
 * <p>
 * Uses jackson code to do the underlying json parsing and generation.
 */
public class JacksonAdapterFactory implements AdapterFactory {

  @Override
  public JsonStreamAdapter create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    return new JacksonAdapter(serializeNulls, serializeEmpty, failOnUnknown);
  }
}
