package org.example;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.json.view.ViewBuilder;
import io.avaje.json.view.ViewBuilderAware;

import java.lang.invoke.MethodHandle;

public class ContactJsonAdapter implements ViewBuilderAware, JsonAdapter<Contact> {

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
  public void build(ViewBuilder builder, String name, MethodHandle mh) {
    builder.beginObject(name, mh);
    builder.add("id", longAdapter, builder.method(Contact.class, "id", Long.class));
    builder.add("firstName", stringAdapter, builder.method(Contact.class, "firstName", String.class));
    builder.add("lastName", stringAdapter, builder.method(Contact.class, "lastName", String.class));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, Contact customer) {
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
  public Contact fromJson(JsonReader reader) {

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
