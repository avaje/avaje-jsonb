package org.example;

import io.avaje.jsonb.*;
import io.avaje.jsonb.spi.ViewBuilder;
import io.avaje.jsonb.spi.ViewBuilderAware;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ContactJsonAdapter extends JsonAdapter<Contact> implements ViewBuilderAware {

  private final JsonAdapter<Long> longAdapter;
  private final JsonAdapter<String> stringAdapter;

  public ContactJsonAdapter(Jsonb jsonb) {
    longAdapter = jsonb.adapter(Long.class);
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
    builder.add("id", longAdapter, lookup.findVirtual(Contact.class, "id", MethodType.methodType(Long.class)));
    builder.add("firstName", stringAdapter, lookup.findVirtual(Contact.class, "firstName", MethodType.methodType(String.class)));
    builder.add("lastName", stringAdapter, lookup.findVirtual(Contact.class, "lastName", MethodType.methodType(String.class)));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, Contact customer) throws IOException {
    writer.beginObject();
    writer.name("id");
    longAdapter.toJson(writer, customer.id());
    writer.name("firstName");
    stringAdapter.toJson(writer, customer.firstName());
    writer.name("lastName");
    stringAdapter.toJson(writer, customer.lastName());
    writer.endObject();
  }

  @Override
  public Contact fromJson(JsonReader reader) throws IOException {

    Long id = null;
    String firstName = null;
    String lastName = null;

    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "id": {
          id = longAdapter.fromJson(reader);
          break;
        }
        case "firstName": {
          firstName = stringAdapter.fromJson(reader);
          break;
        }
        case "lastName": {
          lastName = stringAdapter.fromJson(reader);
          break;
        }
        default: {
          throw new IllegalStateException("fieldName " + fieldName + " not found ");
        }
      }
    }
    reader.endObject();

    return new Contact(id, firstName, lastName);
  }

}
