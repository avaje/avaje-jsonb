package org.example;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

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

    Integer id = null; boolean _set$id = false;
    String name = null; boolean _set$name = false;
    Instant whenCreated = null; boolean _set$whenCreated = false;
    List<Contact> contacts = null; boolean _set$contacts = false;

    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "id": {
          id = intAdapter.fromJson(reader); _set$id = true;
          break;
        }
        case "name": {
          name = stringAdapter.fromJson(reader); _set$name = true;
          break;
        }
        case "whenCreated": {
          whenCreated = instantAdapter.fromJson(reader); _set$whenCreated = true;
          break;
        }
        case "contacts": {
          contacts = contactsAdapter.fromJson(reader); _set$contacts = true;
          break;
        }
        default: {
          // TODO: Support skip unknown field/value etc
          throw new IllegalStateException("fieldName " + fieldName + " not found ");
        }
      }
    }
    reader.endObject();

    Customer customer = new Customer();
    if (_set$id) customer.id(id);
    if (_set$name) customer.name(name);
    if (_set$whenCreated) customer.whenCreated(whenCreated);
    if (_set$contacts) customer.contacts(contacts);
    return customer;
  }

}
