package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonWriter;

public interface BufferedJsonWriter extends JsonWriter {

  String result();
}
