package org.example.other.custom;

import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.PropertyNames;

@CustomAdapter
public class CustomClassJsonAdapter implements JsonAdapter<CustomClass> {

  private final JsonAdapter<String> stringJsonAdapter;
  private final PropertyNames names;

  public CustomClassJsonAdapter(Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
    this.names = jsonb.properties("body");
  }

  @Override
  public void toJson(JsonWriter writer, CustomClass value) {

    writer.beginObject(names);
    writer.name(0);
    stringJsonAdapter.toJson(writer, value.body());
    writer.endObject();
  }

  @Override
  public CustomClass fromJson(JsonReader reader) {

    String val = null;

    // read json
    reader.beginObject(names);
    while (reader.hasNextField()) {
      final String fieldName = reader.nextField();
      if ("body".equals(fieldName)) {
        val = stringJsonAdapter.fromJson(reader);
      } else {
        reader.unmappedField(fieldName);
        reader.skipValue();
      }
    }
    reader.endObject();

    return new CustomClass(val);
  }
}
