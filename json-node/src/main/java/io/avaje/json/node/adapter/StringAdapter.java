package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonString;

final class StringAdapter implements JsonAdapter<JsonString> {

  @Override
  public void toJson(JsonWriter writer, JsonString value) {
    writer.value(value.text());
  }

  @Override
  public JsonString fromJson(JsonReader reader) {
    return JsonString.of(reader.readString());
  }
}
