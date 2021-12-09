package io.avaje.jsonb.jackson;

import io.avaje.jsonb.spi.IOAdapter;
import io.avaje.jsonb.spi.IOAdapterFactory;

/**
 * Jackson Core based adapter.
 * <p>
 * Uses jackson code to do the underlying json parsing and generation.
 */
public class JacksonFactory implements IOAdapterFactory {

  @Override
  public IOAdapter create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    return new JacksonIOAdapter(serializeNulls, serializeEmpty, failOnUnknown);
  }
}
