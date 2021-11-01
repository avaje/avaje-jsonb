package org.example;

import io.avaje.mason.JsonAdapter;
import io.avaje.mason.JsonReader;
import io.avaje.mason.JsonWriter;
import io.avaje.mason.Jsonb;
import io.avaje.mason.base.Types;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class CustomerJsonAdapter extends JsonAdapter<Customer> {

  public static class Register implements Jsonb.Component {
    @Override
    public void register(Jsonb.Builder builder) {
      builder.add(Customer.class, CustomerJsonAdapter::new);
    }
  }

  private final JsonAdapter<Integer> intAdapter;
  private final JsonAdapter<String> stringAdapter;
  private final JsonAdapter<Instant> instantAdapter;
  private final JsonAdapter<List<Contact>> contactsAdapter;

  public CustomerJsonAdapter(Jsonb jsonb) {
    intAdapter = jsonb.adapter(Integer.TYPE);
    stringAdapter = jsonb.adapter(String.class);
    instantAdapter = jsonb.adapter(Instant.class);
    contactsAdapter = jsonb.adapter(Types.listOf(Contact.class));
  }

  @Override
  public void toJson(JsonWriter writer, Customer customer) throws IOException {
    writer.beginObject();
    writer.name("id");
    intAdapter.toJson(writer, customer.id());
    writer.name("name");
    writer.value(customer.name());
    writer.name("whenCreated");
    instantAdapter.toJson(writer, customer.whenCreated());
    writer.name("contacts");
    contactsAdapter.toJson(writer, customer.contacts());
    writer.endObject();
  }

  public void buildFor(PartialContext ctx, Customer customer) {
    //instantAdapter.toJsonFrom(writer, customer::whenCreated);
    if (ctx.include("id")) {
      ctx.add("id", intAdapter, customer::id);
    }
  }

  @Override
  public Customer fromJson(JsonReader reader) throws IOException {

    Integer id = null;
    String name = null;
    Instant whenCreated = null;
    List<Contact> contacts = null;

    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "id": {
          id = intAdapter.fromJson(reader);
          break;
        }
        case "name": {
          name = stringAdapter.fromJson(reader);
          break;
        }
        case "whenCreated": {
          whenCreated = instantAdapter.fromJson(reader);
          break;
        }
        case "contacts": {
          contacts = contactsAdapter.fromJson(reader);
          break;
        }
        default: {
          throw new IllegalStateException("fieldName " + fieldName + " not found ");
        }
      }
    }
    reader.endObject();

    Customer customer = new Customer();
    customer.id(id);
    customer.name(name);
    customer.whenCreated(whenCreated);
    customer.contacts(contacts);
    return customer;
  }

}
