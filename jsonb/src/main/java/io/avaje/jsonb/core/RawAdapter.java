package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

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
