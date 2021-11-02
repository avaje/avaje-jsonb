package io.avaje.jsonb.core;

import io.avaje.jsonb.Jsonb;

public class DefaultBootstrap {

  public static Jsonb.Builder newBuilder() {
    return new DefaultJsonb.DBuilder();
  }
}
