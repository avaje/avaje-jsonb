package io.avaje.jsonb.core;

import io.avaje.jsonb.Jsonb;

/**
 * Default bootstrap of Jsonb.
 */
public final class DefaultBootstrap {

  /**
   * Create the Jsonb.Builder.
   */
  public static Jsonb.Builder builder() {
    return new DJsonb.DBuilder();
  }

  public static Jsonb defaultInstance() {
    return DJsonb.DBuilder.DEFAULT;
  }
}
