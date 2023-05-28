package org.example.other.custom;

import java.util.Map.Entry;

import java.util.AbstractMap.SimpleImmutableEntry;
import io.avaje.jsonb.*;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.PropertyNames;

@CustomAdapter
public class CustomJsonAdapter implements JsonAdapter<Entry> {

  private final JsonAdapter<String> stringJsonAdapter;
  private final PropertyNames names;

  public CustomJsonAdapter(Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
    this.names = jsonb.properties("key", "val");
  }

  @Override
  public void toJson(JsonWriter writer, Entry value) {

    writer.beginObject(names);
    writer.name(0);
    stringJsonAdapter.toJson(writer, (String) value.getKey());
    writer.name(1);
    stringJsonAdapter.toJson(writer, (String) value.getValue());
    writer.endObject();
  }

  @Override
  public Entry<String, String> fromJson(JsonReader reader) {
    // variables to read json values into, constructor params don't need _set$ flags
    String key = null;
    String val = null;

    // read json
    reader.beginObject(names);
    while (reader.hasNextField()) {
      final String fieldName = reader.nextField();
      switch (fieldName) {
        case "key":
          key = stringJsonAdapter.fromJson(reader);
          break;
        case "val":
          val = stringJsonAdapter.fromJson(reader);
          break;

        default:
          reader.unmappedField(fieldName);
          reader.skipValue();
      }
    }
    reader.endObject();

    return new SimpleImmutableEntry<>(key, val);
  }
}
