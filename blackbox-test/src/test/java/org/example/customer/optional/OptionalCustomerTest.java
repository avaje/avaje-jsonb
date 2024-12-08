package org.example.customer.optional;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.example.customer.Contact;
import org.example.customer.Customer;
import org.junit.jupiter.api.Test;

import io.avaje.json.*;
import io.avaje.jsonb.*;

class OptionalCustomerTest {

  final String jsonStart = "{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\",\"whenCreated\":";

  Jsonb jsonb = Jsonb.builder().serializeEmpty(false).build();

  @Test
  void anyToJson() {
    final var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);

    final String asJson = jsonb.toJson(Optional.of(customer));
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\"}");
  }


  @Test
  void toJson() {

    final var customerOp = customer();
    final var customer = customerOp.orElseThrow();
    final JsonType<Optional<Customer>> customerType = jsonb.type(Types.optionalOf(Customer.class));
    final String asJson = customerType.toJson(customerOp);
    assertThat(asJson).startsWith(jsonStart);
    assertThat(asJson).contains("\"contacts\":[");

    final Customer from2 = customerType.fromJson(asJson).orElseThrow();
    assertThat(from2.id()).isEqualTo(customer.id());
    assertThat(from2.name()).isEqualTo(customer.name());
    assertThat(from2.status()).isEqualTo(customer.status());
    assertThat(from2.whenCreated()).isEqualTo(customer.whenCreated());

    // using JsonAdapter ... not so easy to deal with Strings

    final StringWriter writer = new StringWriter();
    final JsonWriter jsonWriter = jsonb.writer(writer);
    final JsonAdapter<Optional<Customer>> customerAdapter = jsonb.adapter(Types.optionalOf(Customer.class));
    customerAdapter.toJson(jsonWriter, customerOp);
    jsonWriter.close();

    assertThat(writer.toString()).startsWith(jsonStart);

    final Customer from1 = customerAdapter.fromJson(jsonb.reader(writer.toString())).orElseThrow();
    assertThat(from1.id()).isEqualTo(customer.id());
    assertThat(from1.name()).isEqualTo(customer.name());
    assertThat(from1.whenCreated()).isEqualTo(customer.whenCreated());
  }

  private Optional<Customer> customer() {
    final var customer =
        new Customer()
            .id(42L)
            .name("rob")
            .status(Customer.Status.ACTIVE)
            .whenCreated(Instant.now());
    customer.contacts().add(new Contact(UUID.randomUUID(), "fo", "nar"));
    customer.contacts().add(new Contact(UUID.randomUUID(), "ba", "zar"));
    return Optional.of(customer);
  }

  @Test
  void fromObject() {

    final JsonType<Optional<Customer>> customerJson = jsonb.type(Types.optionalOf(Customer.class));

    final Instant now = Instant.now();
    final Map<String, Object> customerMap = new LinkedHashMap<>();
    customerMap.put("id", 42L);
    customerMap.put("name", "foo");
    customerMap.put("whenCreated", now);

    final Set<Map<String, Object>> contactMaps = new LinkedHashSet<>();
    contactMaps.add(contactMap("fn0", "ln0"));
    contactMaps.add(contactMap("fn1", "ln1"));
    customerMap.put("contacts", contactMaps);

    final Customer customer = customerJson.fromObject(customerMap).orElseThrow();

    assertThat(customer.id()).isEqualTo(42L);
    assertThat(customer.name()).isEqualTo("foo");
    assertThat(customer.whenCreated()).isEqualTo(now);
    assertThat(customer.contacts()).hasSize(2);
    assertThat(customer.contacts().get(0).firstName()).isEqualTo("fn0");
    assertThat(customer.contacts().get(1).firstName()).isEqualTo("fn1");
  }

  private Map<String, Object> contactMap(String fn, String ln) {
    final Map<String, Object> contactMap = new LinkedHashMap<>();
    contactMap.put("id", UUID.randomUUID());
    contactMap.put("firstName", fn);
    contactMap.put("lastName", ln);
    return contactMap;
  }

  @Test
  void toJson_viaTypeObject() {

    final Object customerAsObject = Optional.of(new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE));

    final String asJson = jsonb.type(Object.class).toJson(customerAsObject);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\"}");

    final Customer from1 = jsonb.<Optional<Customer>>type(Types.optionalOf(Customer.class)).fromJson(asJson).orElseThrow();
    assertThat(from1.id()).isEqualTo(42L);
    assertThat(from1.name()).isEqualTo("rob");
    assertThat(from1.status()).isEqualTo(Customer.Status.ACTIVE);
  }

}
