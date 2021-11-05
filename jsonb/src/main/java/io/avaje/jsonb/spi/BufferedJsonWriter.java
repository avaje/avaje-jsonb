package io.avaje.jsonb.spi;

import io.avaje.jsonb.JsonWriter;

import java.io.IOException;

public interface BufferedJsonWriter extends JsonWriter {

  String result() throws IOException;
}
