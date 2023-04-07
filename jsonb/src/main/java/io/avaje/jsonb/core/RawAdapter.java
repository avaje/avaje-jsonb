package io.avaje.jsonb.core;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;

final class RawAdapter {

  static final JsonAdapter<String> STR = new Str().nullSafe();

  private static final class Str implements JsonAdapter<String> {

    @Override
    public void toJson(JsonWriter writer, String value) {
      if ("null".equalsIgnoreCase(value)) {
        writer.nullValue();
      } else {
        writer.rawValue(value);
      }
    }

    @Override
    public String fromJson(JsonReader reader) {
      return reader.readRaw();
    }
  }
}
