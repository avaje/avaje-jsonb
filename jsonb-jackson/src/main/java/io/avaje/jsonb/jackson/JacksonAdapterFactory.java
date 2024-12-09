package io.avaje.jsonb.jackson;

import io.avaje.json.stream.JsonStream;
import io.avaje.jsonb.spi.JsonStreamFactory;

/**
 * Jackson Core based adapter.
 * <p>
 * Uses jackson code to do the underlying json parsing and generation.
 */
public class JacksonAdapterFactory implements JsonStreamFactory {

  @Override
  public JsonStream create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    return new JacksonAdapter(serializeNulls, serializeEmpty, failOnUnknown);
  }
}
