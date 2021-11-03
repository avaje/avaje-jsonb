package org.example.customer;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import org.example.customer.jsonb.ContactJsonAdapter;
import org.example.customer.jsonb.CustomerJsonAdapter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

  final String jsonStart = "{\"id\":42,\"name\":\"rob\",\"whenCreated\":";

  @Test
  void toJson() throws IOException {

    Jsonb jsonb = Jsonb.newBuilder()
      .add(Contact.class, ContactJsonAdapter::new)
      .add(Customer.class, CustomerJsonAdapter::new)
      .build();

    Customer customer = new Customer().id(42L).name("rob").whenCreated(Instant.now());
    customer.contacts().add(new Contact(UUID.randomUUID(), "fo", "nar"));
    customer.contacts().add(new Contact(UUID.randomUUID(), "ba", "zar"));


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

}
