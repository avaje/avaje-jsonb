package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonBoolean;

final class BooleanAdapter implements JsonAdapter<JsonBoolean> {

  @Override
  public void toJson(JsonWriter writer, JsonBoolean node) {
    writer.value(node.value());
  }

  @Override
  public JsonBoolean fromJson(JsonReader reader) {
    return JsonBoolean.of(reader.readBoolean());
  }
}
