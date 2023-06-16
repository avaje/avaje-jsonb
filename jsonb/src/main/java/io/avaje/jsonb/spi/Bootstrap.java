package io.avaje.jsonb.spi;

import io.avaje.jsonb.Jsonb;

/**
 * Bootstrap Jsonb.
 */
public interface Bootstrap {

  /**
   * Create and return a Builder (with an underling SPI implementation).
   * <p>
   */
  Jsonb.Builder builder();
}
