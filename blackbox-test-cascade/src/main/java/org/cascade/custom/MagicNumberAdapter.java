package org.cascade.custom;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;

@CustomAdapter
public class MagicNumberAdapter implements JsonAdapter<MagicNumber> {

  public MagicNumberAdapter(Jsonb jsonb) {}

  @Override
  public void toJson(JsonWriter writer, MagicNumber value) {
    writer.value(value.number());
  }

  @Override
  public MagicNumber fromJson(JsonReader reader) {
    return new MagicNumber(reader.readInt());
  }
}