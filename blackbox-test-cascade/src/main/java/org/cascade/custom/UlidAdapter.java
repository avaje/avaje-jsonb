package org.cascade.custom;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;

@CustomAdapter
public class UlidAdapter implements JsonAdapter<Ulid> {

  public UlidAdapter(Jsonb jsonb) {}

  @Override
  public void toJson(JsonWriter writer, Ulid ulid) {
    writer.value(ulid.toString());
  }

  @Override
  public Ulid fromJson(JsonReader reader) {
    return Ulid.from(reader.readString());
  }
}