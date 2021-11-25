package io.avaje.jsonb.core;

import io.avaje.jsonb.Jsonb;

/**
 * Default bootstrap of Jsonb.
 */
public class DefaultBootstrap {

  /**
   * Create the Jsonb.Builder.
   */
  public static Jsonb.Builder newBuilder() {
    return new DJsonb.DBuilder();
  }
}
