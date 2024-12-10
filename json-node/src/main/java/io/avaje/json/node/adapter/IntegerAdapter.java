package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonInteger;

final class IntegerAdapter implements JsonAdapter<JsonInteger> {

  @Override
  public void toJson(JsonWriter writer, JsonInteger value) {
    writer.value(value.intValue());
  }

  @Override
  public JsonInteger fromJson(JsonReader reader) {
    return JsonInteger.of(reader.readInt());
  }
}
