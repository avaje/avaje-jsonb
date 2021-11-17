package org.example;

import io.avaje.jsonb.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

  final String jsonStart = "{\"id\":42,\"name\":\"rob\",\"whenCreated\":";

  @Test
  void toJson() throws IOException {

    Jsonb jsonb = Jsonb.newBuilder().build();

    Address billingAddress = new Address().street("street").suburb("suburb");
    Customer customer = new Customer().id(42).name("rob").whenCreated(Instant.now()).billingAddress(billingAddress);
    customer.contacts().add(new Contact(7L, "fo", "nar"));
    customer.contacts().add(new Contact(8L, "ba", "zar"));


    JsonType<Customer> customerType = jsonb.type(Customer.class);
    String asJson = customerType.toJson(customer);
    assertThat(asJson).startsWith(jsonStart);
    assertThat(asJson).contains("\"contacts\":[");

    Customer from2 = customerType.fromJson(asJson);
    assertThat(from2.id()).isEqualTo(customer.id());
    assertThat(from2.name()).isEqualTo(customer.name());
    assertThat(from2.whenCreated()).isEqualTo(customer.whenCreated());

    // using JsonAdapter ... not so easy to deal with Strings

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = jsonb.writer(writer);
    JsonAdapter<Customer> customerAdapter = jsonb.adapter(Customer.class);
    customerAdapter.toJson(jsonWriter, customer);
    jsonWriter.close();

    assertThat(writer.toString()).startsWith(jsonStart);

    Customer from1 = customerAdapter.fromJson(jsonb.reader(writer.toString()));
    assertThat(from1.id()).isEqualTo(customer.id());
    assertThat(from1.name()).isEqualTo(customer.name());
    assertThat(from1.whenCreated()).isEqualTo(customer.whenCreated());
  }

  @Test
  void jsonView() {

    Jsonb jsonb = Jsonb.newBuilder().build();

    JsonView<Customer> customerJsonView = jsonb.type(Customer.class).view("(id, name, billingAddress(street), contacts(id, lastName))");

    Address billingAddress = new Address().street("my street").suburb("my suburb");
    Customer customer = new Customer().id(42).name("rob").whenCreated(Instant.now()).billingAddress(billingAddress);
    customer.contacts().add(new Contact(7L, "fo", "nar"));
    customer.contacts().add(new Contact(8L, "ba", "zar"));

    String asJson = customerJsonView.toJson(customer);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"billingAddress\":{\"street\":\"my street\"},\"contacts\":[{\"id\":7,\"lastName\":\"nar\"},{\"id\":8,\"lastName\":\"zar\"}]}");
  }

  @Test
  void jsonView_list() {

    Jsonb jsonb = Jsonb.newBuilder().build();

    JsonView<List<Customer>> customerJsonView = jsonb.type(Customer.class).list().view("(id, name, contacts(*))");

    Address billingAddress = new Address().street("my street").suburb("my suburb");
    Customer customer0 = new Customer().id(42).name("rob").whenCreated(Instant.now()).billingAddress(billingAddress);
    Customer customer1 = new Customer().id(43).name("bob").whenCreated(Instant.now()).billingAddress(billingAddress);
    customer0.contacts().add(new Contact(7L, "fo", "nar"));
    customer0.contacts().add(new Contact(8L, "ba", "zar"));

    List<Customer> customers = new ArrayList<>();
    customers.add(customer0);
    customers.add(customer1);

    String asJson = customerJsonView.toJson(customers);
    assertThat(asJson).isEqualTo("[{\"id\":42,\"name\":\"rob\",\"contacts\":[{\"id\":7,\"firstName\":\"fo\",\"lastName\":\"nar\"},{\"id\":8,\"firstName\":\"ba\",\"lastName\":\"zar\"}]},{\"id\":43,\"name\":\"bob\"}]");
  }
}
