package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonLong;

final class LongAdapter implements JsonAdapter<JsonLong> {

  @Override
  public void toJson(JsonWriter writer, JsonLong value) {
    writer.value(value.longValue());
  }

  @Override
  public JsonLong fromJson(JsonReader reader) {
    return JsonLong.of(reader.readLong());
  }
}
