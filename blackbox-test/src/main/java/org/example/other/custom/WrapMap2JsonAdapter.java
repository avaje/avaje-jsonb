package org.example.other.custom;

import java.util.Map;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

@CustomAdapter
public class WrapMap2JsonAdapter implements JsonAdapter<WrapMap2> {
  private final JsonAdapter<Map<String, String>> stringMapJsonAdapter;

  public WrapMap2JsonAdapter(Jsonb jsonb) {
    this.stringMapJsonAdapter =
        jsonb.adapter(Types.newParameterizedType(Map.class, String.class, String.class));
  }

  @Override
  public WrapMap2 fromJson(JsonReader reader) {
    return new WrapMap2(stringMapJsonAdapter.fromJson(reader));
  }

  @Override
  public void toJson(JsonWriter writer, WrapMap2 wrapMap) {
    writer.beginObject();
    wrapMap.forEach(
        (key, value) -> {
          writer.name(key);
          writer.value(value);
        });
    writer.endObject();
  }
}
