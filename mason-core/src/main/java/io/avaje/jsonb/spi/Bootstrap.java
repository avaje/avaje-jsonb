package io.avaje.jsonb.spi;

import io.avaje.jsonb.Jsonb;

public interface Bootstrap {

  Jsonb.Builder newBuilder();
}
