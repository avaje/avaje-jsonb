package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.generator.models.valid.Example3Packet.Example2Packet;

@CustomAdapter
public class CustomJsonAdapter implements JsonAdapter<io.avaje.jsonb.generator.models.valid.Example3Packet.Example2Packet> {

  @Override
  public void toJson(JsonWriter writer, Example2Packet value) {}

  @Override
  public Example2Packet fromJson(JsonReader reader) {
    return null;
  }
}
