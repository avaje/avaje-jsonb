package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;

@CustomAdapter
public class CustomJsonAdapter implements JsonAdapter<Example3Packet> {

  public CustomJsonAdapter(Jsonb jsonb) {}

  @Override
  public void toJson(JsonWriter writer, Example3Packet value) {}

  @Override
  public Example3Packet fromJson(JsonReader reader) {
    return null;
  }
}
