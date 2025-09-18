package org.example.other.custom;

import java.util.Map;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

@CustomAdapter
public class WrapMapJsonAdapter implements JsonAdapter<WrapMap> {
  private final JsonAdapter<Map<String, String>> stringMapJsonAdapter;

  public WrapMapJsonAdapter(Jsonb jsonb) {
    this.stringMapJsonAdapter =
        jsonb.adapter(Types.newParameterizedType(Map.class, String.class, String.class));
  }

  @Override
  public WrapMap fromJson(JsonReader reader) {
    return new WrapMap(stringMapJsonAdapter.fromJson(reader));
  }

  @Override
  public void toJson(JsonWriter writer, WrapMap wrapMap) {
    writer.beginObject();
    wrapMap.forEach(
        (key, value) -> {
          writer.name(key);
          writer.value(value);
        });
    writer.endObject();
  }
}
