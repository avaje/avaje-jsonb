package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonArray;
import io.avaje.json.node.JsonNode;

import java.util.ArrayList;
import java.util.List;

final class ArrayAdapter implements JsonAdapter<JsonArray> {

  private final JsonAdapter<JsonNode> elementAdapter;

  ArrayAdapter(JsonAdapter<JsonNode> elementAdapter) {
    this.elementAdapter = elementAdapter;
  }

  @Override
  public JsonArray fromJson(JsonReader reader) {
    List<JsonNode> result = new ArrayList<>();
    reader.beginArray();
    while (reader.hasNextElement()) {
      result.add(elementAdapter.fromJson(reader));
    }
    reader.endArray();
    return JsonArray.of(result);
  }

  @Override
  public void toJson(JsonWriter writer, JsonArray value) {
    if (value.isEmpty()) {
      writer.emptyArray();
      return;
    }
    writer.beginArray();
    for (JsonNode element : value.elements()) {
      elementAdapter.toJson(writer, element);
    }
    writer.endArray();
  }

}
