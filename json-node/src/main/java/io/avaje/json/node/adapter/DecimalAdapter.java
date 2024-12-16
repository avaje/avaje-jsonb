package io.avaje.json.node.adapter;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonDecimal;

final class DecimalAdapter implements JsonAdapter<JsonDecimal> {

  @Override
  public void toJson(JsonWriter writer, JsonDecimal value) {
    writer.value(value.decimalValue());
  }

  @Override
  public JsonDecimal fromJson(JsonReader reader) {
    return JsonDecimal.of(reader.readDecimal());
  }
}
