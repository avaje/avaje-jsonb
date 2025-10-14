package io.avaje.jsonb.jackson;

import io.avaje.json.stream.JsonStream;
import io.avaje.jsonb.spi.JsonStreamFactory;
import io.avaje.spi.ServiceProvider;

/**
 * Jackson Core based adapter.
 * <p>
 * Uses jackson code to do the underlying json parsing and generation.
 */
@ServiceProvider
public class JacksonAdapterFactory implements JsonStreamFactory {

  @Override
  public JsonStream create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    return new JacksonAdapter(serializeNulls, serializeEmpty, failOnUnknown);
  }
}
