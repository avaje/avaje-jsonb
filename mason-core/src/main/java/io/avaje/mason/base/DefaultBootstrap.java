package io.avaje.mason.base;

import io.avaje.mason.Jsonb;

public class DefaultBootstrap {

  public static Jsonb.Builder newBuilder() {
    return new DefaultJsonb.DBuilder();
  }
}
