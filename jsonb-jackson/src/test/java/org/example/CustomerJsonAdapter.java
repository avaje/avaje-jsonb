package org.example;

import io.avaje.jsonb.*;
import io.avaje.json.*;
import io.avaje.json.view.ViewBuilder;
import io.avaje.json.view.ViewBuilderAware;

import java.lang.invoke.MethodHandle;
import java.time.Instant;
import java.util.List;

public class CustomerJsonAdapter implements ViewBuilderAware, JsonAdapter<Customer> {

  private final JsonAdapter<Integer> intAdapter;
  private final JsonAdapter<String> stringAdapter;
  private final JsonAdapter<Instant> instantAdapter;
  private final JsonAdapter<List<Contact>> contactsAdapter;
  private final JsonAdapter<Address> addressAdapter;
  private final PropertyNames names;

  public CustomerJsonAdapter(Jsonb jsonb) {
    intAdapter = jsonb.adapter(Integer.TYPE);
    stringAdapter = jsonb.adapter(String.class);
    instantAdapter = jsonb.adapter(Instant.class);
    addressAdapter = jsonb.adapter(Address.class).nullSafe();
    contactsAdapter = jsonb.adapter(Types.listOf(Contact.class));
    names = jsonb.properties("id", "name", "whenCreated", "billingAddress", "contacts");
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
  public void build(ViewBuilder builder, String name, MethodHandle handle) {
    builder.beginObject(name, handle);
    builder.add("id", intAdapter, builder.method(Customer.class, "id", Integer.class));
    builder.add("name", stringAdapter, builder.method(Customer.class, "name", String.class));
    builder.add("whenCreated", instantAdapter, builder.method(Customer.class, "whenCreated", Instant.class));
    builder.add("billingAddress", addressAdapter, builder.method(Customer.class, "billingAddress", Address.class));
    builder.add("contacts", contactsAdapter, builder.method(Customer.class, "contacts", List.class));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, Customer customer) {
    writer.beginObject(names);
    writer.name( 0);
    intAdapter.toJson(writer, customer.id());
    writer.name( 1);
    stringAdapter.toJson(writer, customer.name());
    writer.name( 2);
    instantAdapter.toJson(writer, customer.whenCreated());
    writer.name( 3);
    addressAdapter.toJson(writer, customer.billingAddress());
    writer.name( 4);
    contactsAdapter.toJson(writer, customer.contacts());
    writer.endObject();
  }

  @Override
  public Customer fromJson(JsonReader reader) {

    Integer id = null; boolean _set$id = false;
    String name = null; boolean _set$name = false;
    Instant whenCreated = null; boolean _set$whenCreated = false;
    Address billingAddress = null; boolean _set$billingAddress = false;
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
        case "billingAddress": {
          billingAddress = addressAdapter.fromJson(reader); _set$billingAddress = true;
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
    if (_set$billingAddress) customer.billingAddress(billingAddress);
    if (_set$contacts) customer.contacts(contacts);
    return customer;
  }

}
