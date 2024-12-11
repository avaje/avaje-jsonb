package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonDouble;

final class DoubleAdapter implements JsonAdapter<JsonDouble> {

  @Override
  public void toJson(JsonWriter writer, JsonDouble value) {
    writer.value(value.doubleValue());
  }

  @Override
  public JsonDouble fromJson(JsonReader reader) {
    return JsonDouble.of(reader.readDouble());
  }
}
