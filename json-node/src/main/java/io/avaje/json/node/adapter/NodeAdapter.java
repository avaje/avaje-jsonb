package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.*;

final class NodeAdapter implements JsonAdapter<JsonNode>  {

  private final BooleanAdapter booleanAdapter;
  private final StringAdapter stringAdapter;
  private final ArrayAdapter arrayAdapter;
  private final ObjectAdapter objectAdapter;
  private final JsonAdapter<JsonNumber> numberAdapter;

  NodeAdapter(JsonAdapter<JsonNumber> numberAdapter) {
    this.booleanAdapter = DJsonNodeMapper.BOOLEAN_ADAPTER;
    this.stringAdapter = DJsonNodeMapper.STRING_ADAPTER;
    this.numberAdapter = numberAdapter;
    this.arrayAdapter = new ArrayAdapter(this);
    this.objectAdapter = new ObjectAdapter(this);
  }

  ArrayAdapter arrayAdapter() {
    return arrayAdapter;
  }

  ObjectAdapter objectAdapter() {
    return objectAdapter;
  }

  @Override
  public JsonNode fromJson(JsonReader reader) {
    switch (reader.currentToken()) {
      case NULL:
        reader.isNullValue();
        return null;
      case BEGIN_ARRAY:
        return arrayAdapter.fromJson(reader);
      case BEGIN_OBJECT:
        return objectAdapter.fromJson(reader);
      case STRING:
        return stringAdapter.fromJson(reader);
      case BOOLEAN:
        return booleanAdapter.fromJson(reader);
      case NUMBER:
        return numberAdapter.fromJson(reader);
      default:
        throw new IllegalStateException("Expected a value but was " + reader.currentToken() + " at path " + reader.location());
    }
  }

  @Override
  public void toJson(JsonWriter writer, JsonNode value) {
    if (value == null) {
      writer.nullValue();
      return;
    }
    switch (value.type()) {
      case NULL:
        writer.nullValue();
        break;
      case ARRAY:
        arrayAdapter.toJson(writer, (JsonArray)value);
        break;
      case OBJECT:
        objectAdapter.toJson(writer, (JsonObject) value);
        break;
      case BOOLEAN:
        booleanAdapter.toJson(writer, (JsonBoolean) value);
        break;
      case STRING:
        stringAdapter.toJson(writer, (JsonString)value);
        break;
      case NUMBER:
        numberAdapter.toJson(writer, (JsonNumber)value);
        break;
      default:
        throw new UnsupportedOperationException("Type not supported " + value.getClass());
    }
  }

  @Override
  public String toString() {
    return "JsonNodeAdapter";
  }
}
