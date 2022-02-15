package io.avaje.jsonb.jakarta;

import io.avaje.jsonb.spi.JsonStreamAdapter;
import io.avaje.jsonb.spi.AdapterFactory;

/**
 * Factory building the adapter for jakarta json.
 */
public final class JakartaAdapterFactory implements AdapterFactory {

  @Override
  public JsonStreamAdapter create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    return new JakartaIOAdapter(serializeNulls, serializeEmpty, failOnUnknown);
  }

}
