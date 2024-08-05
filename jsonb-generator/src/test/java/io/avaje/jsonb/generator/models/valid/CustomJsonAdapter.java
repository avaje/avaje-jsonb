package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.generator.models.valid.Example3Packet.Example2Packet;

@CustomAdapter
public class CustomJsonAdapter implements JsonAdapter<Example2Packet> {

  @Override
  public void toJson(JsonWriter writer, Example2Packet value) {}

  @Override
  public Example2Packet fromJson(JsonReader reader) {
    return null;
  }
}
