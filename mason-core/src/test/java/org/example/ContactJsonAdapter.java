package org.example;

import io.avaje.mason.JsonAdapter;
import io.avaje.mason.JsonReader;
import io.avaje.mason.JsonWriter;
import io.avaje.mason.Jsonb;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

public class ContactJsonAdapter extends JsonAdapter<Contact> {

  public static class Register implements Jsonb.Component {
    @Override
    public void register(Jsonb.Builder builder) {
      builder.add(Contact.class, ContactJsonAdapter::new);
    }
  }

  private final JsonAdapter<UUID> uuidAdapter;
  private final JsonAdapter<String> stringAdapter;

  public ContactJsonAdapter(Jsonb jsonb) {
    uuidAdapter = jsonb.adapter(UUID.class);
    stringAdapter = jsonb.adapter(String.class);
  }

  @Override
  public void toJson(JsonWriter writer, Contact customer) throws IOException {
    writer.beginObject();
    writer.name("id");
    uuidAdapter.toJson(writer, customer.id());
    writer.name("firstName");
    writer.value(customer.firstName());
    writer.name("lastName");
    writer.value(customer.lastName());
    writer.endObject();
  }

  @Override
  public Contact fromJson(JsonReader reader) throws IOException {

    UUID id = null;
    String firstName = null;
    String lastName = null;

    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "id": {
          id = uuidAdapter.fromJson(reader);
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
