package io.avaje.mason.spi;

import io.avaje.mason.Jsonb;

public interface Bootstrap {

  Jsonb.Builder newBuilder();
}
