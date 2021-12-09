package io.avaje.jsonb.jakarta;

import io.avaje.jsonb.spi.IOAdapter;
import io.avaje.jsonb.spi.IOAdapterFactory;

/**
 * Factory building the adapter for jakarta json.
 */
public final class JakartaAdapterFactory implements IOAdapterFactory {

  @Override
  public IOAdapter create(boolean serializeNulls, boolean serializeEmpty, boolean failOnUnknown) {
    return new JakartaJsonAdapter(serializeNulls, serializeEmpty, failOnUnknown);
  }

}
