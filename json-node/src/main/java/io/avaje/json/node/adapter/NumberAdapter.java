package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonDouble;
import io.avaje.json.node.JsonLong;
import io.avaje.json.node.JsonNumber;

final class NumberAdapter implements JsonAdapter<JsonNumber> {

  @Override
  public void toJson(JsonWriter writer, JsonNumber value) {
    value.toJson(writer);
  }

  @Override
  public JsonNumber fromJson(JsonReader reader) {
    // read unknown number type
    double d = reader.readDouble();
    if (d % 1 == 0) {
      return JsonLong.of((long) d);
    }
    return JsonDouble.of(d);
  }
}
