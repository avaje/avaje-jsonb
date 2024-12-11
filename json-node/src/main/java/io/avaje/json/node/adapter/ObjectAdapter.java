package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonNode;
import io.avaje.json.node.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts maps with string keys to JSON objects.
 */
final class ObjectAdapter implements JsonAdapter<JsonObject> {

  private final JsonAdapter<JsonNode> valueAdapter;

  ObjectAdapter(JsonAdapter<JsonNode> valueAdapter) {
    this.valueAdapter = valueAdapter;
  }

  @Override
  public void toJson(JsonWriter writer, JsonObject value) {
    writer.beginObject();
    for (var entry : value.elements().entrySet()) {
      if (entry.getKey() == null) {
        throw new JsonDataException("Map key is null at " + writer.path());
      }
      writer.name(entry.getKey());
      valueAdapter.toJson(writer, entry.getValue());
    }
    writer.endObject();
  }

  @Override
  public JsonObject fromJson(JsonReader reader) {
    Map<String, JsonNode> result = new LinkedHashMap<>();
    reader.beginObject();
    while (reader.hasNextField()) {
      String name = reader.nextField();
      JsonNode value = valueAdapter.fromJson(reader);
      JsonNode replaced = result.put(name, value);
      if (replaced != null) {
        throw new JsonDataException(String.format("Map key '%s' has multiple values at path %s : %s and %s", name, reader.location(), replaced, value));
      }
    }
    reader.endObject();
    return JsonObject.of(result);
  }

  @Override
  public String toString() {
    return "JsonObject()";
  }
}
