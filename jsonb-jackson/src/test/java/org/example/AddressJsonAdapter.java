package org.example;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.PropertyNames;
import io.avaje.jsonb.Jsonb;
import io.avaje.json.view.ViewBuilder;
import io.avaje.json.view.ViewBuilderAware;

import java.util.function.UnaryOperator;

public class AddressJsonAdapter implements ViewBuilderAware, JsonAdapter<Address> {

  private final JsonAdapter<String> stringAdapter;
  private final PropertyNames names;

  public AddressJsonAdapter(Jsonb jsonb) {
    stringAdapter = jsonb.adapter(String.class);
    names = jsonb.properties("street", "suburb", "city");
  }

  @Override
  public boolean isViewBuilderAware() {
    return true;
  }

  @Override
  public ViewBuilderAware viewBuild() {
    return this;
  }

  @Override
  public void build(ViewBuilder builder, String name, UnaryOperator<Object> accessor) {
    builder.beginObject(name, accessor);
    builder.add("street", stringAdapter, o -> ((Address) o).street());
    builder.add("suburb", stringAdapter, o -> ((Address) o).suburb());
    builder.add("city", stringAdapter, o -> ((Address) o).city());
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, Address address) {
    writer.beginObject(names);
    writer.name(0);
    stringAdapter.toJson(writer, address.street());
    writer.name(1);
    stringAdapter.toJson(writer, address.suburb());
    writer.name(2);
    stringAdapter.toJson(writer, address.city());
    writer.endObject();
  }

  @Override
  public Address fromJson(JsonReader reader) {

    Address address = new Address();

    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "street": {
          address.street(stringAdapter.fromJson(reader));
          break;
        }
        case "suburb": {
          address.suburb(stringAdapter.fromJson(reader));
          break;
        }
        case "city": {
          address.city(stringAdapter.fromJson(reader));
          break;
        }
        default: {
          throw new IllegalStateException("fieldName " + fieldName + " not found ");
        }
      }
    }
    reader.endObject();
    return address;
  }

}
