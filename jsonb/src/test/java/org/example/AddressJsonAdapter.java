package org.example;

import io.avaje.jsonb.*;
import io.avaje.jsonb.spi.ViewBuilder;
import io.avaje.jsonb.spi.ViewBuilderAware;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class AddressJsonAdapter extends JsonAdapter<Address> implements ViewBuilderAware {

  private final JsonAdapter<String> stringAdapter;

  public AddressJsonAdapter(Jsonb jsonb) {
    stringAdapter = jsonb.adapter(String.class);
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
  public void build(ViewBuilder builder, String name, MethodHandle mh) throws NoSuchMethodException, IllegalAccessException {
    builder.beginObject(name, mh);
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    builder.add("street", stringAdapter, lookup.findVirtual(Address.class, "street", MethodType.methodType(String.class)));
    builder.add("suburb", stringAdapter, lookup.findVirtual(Address.class, "suburb", MethodType.methodType(String.class)));
    builder.add("city", stringAdapter, lookup.findVirtual(Address.class, "city", MethodType.methodType(String.class)));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, Address address) throws IOException {
    writer.beginObject();
    writer.name("street");
    stringAdapter.toJson(writer, address.street());
    writer.name("suburb");
    stringAdapter.toJson(writer, address.suburb());
    writer.name("city");
    stringAdapter.toJson(writer, address.city());
    writer.endObject();
  }

  @Override
  public Address fromJson(JsonReader reader) throws IOException {

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
