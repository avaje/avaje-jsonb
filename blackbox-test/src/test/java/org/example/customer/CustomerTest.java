package org.example.customer;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
//import org.example.customer.jsonb.ContactJsonAdapter;
//import org.example.customer.jsonb.CustomerJsonAdapter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

  final String jsonStart = "{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\",\"whenCreated\":";

  @Test
  void toJson() throws IOException {

    Jsonb jsonb = Jsonb.newBuilder()
      // the below adapters are automatically registered via
      // service loading the GeneratedJsonComponent
      //.add(Contact.class, ContactJsonAdapter::new)
      //.add(Customer.class, CustomerJsonAdapter::new)
      .build();

    var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE).whenCreated(Instant.now());
    customer.contacts().add(new Contact(UUID.randomUUID(), "fo", "nar"));
    customer.contacts().add(new Contact(UUID.randomUUID(), "ba", "zar"));


    JsonType<Customer> customerType = jsonb.type(Customer.class);
    String asJson = customerType.toJson(customer);
    assertThat(asJson).startsWith(jsonStart);
    assertThat(asJson).contains("\"contacts\":[");

    Customer from2 = customerType.fromJson(asJson);
    assertThat(from2.id()).isEqualTo(customer.id());
    assertThat(from2.name()).isEqualTo(customer.name());
    assertThat(from2.status()).isEqualTo(customer.status());
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
  void fromObject() throws IOException {
    var jsonb = Jsonb.newBuilder().build();

    var customerJson = jsonb.type(Customer.class);

    Instant now = Instant.now();
    Map<String,Object> customerMap = new LinkedHashMap<>();
    customerMap.put("id", 42L);
    customerMap.put("name", "foo");
    customerMap.put("whenCreated", now);

    Set<Map<String,Object>> contactMaps = new LinkedHashSet<>();
    contactMaps.add(contactMap("fn0", "ln0"));
    contactMaps.add(contactMap("fn1", "ln1"));
    customerMap.put("contacts", contactMaps);

    Customer customer = customerJson.fromObject(customerMap);

    assertThat(customer.id()).isEqualTo(42L);
    assertThat(customer.name()).isEqualTo("foo");
    assertThat(customer.whenCreated()).isEqualTo(now);
    assertThat(customer.contacts()).hasSize(2);
    assertThat(customer.contacts().get(0).firstName()).isEqualTo("fn0");
    assertThat(customer.contacts().get(1).firstName()).isEqualTo("fn1");
  }

  private Map<String, Object> contactMap(String fn, String ln) {
    Map<String,Object> contactMap = new LinkedHashMap<>();
    contactMap.put("id", UUID.randomUUID());
    contactMap.put("firstName", fn);
    contactMap.put("lastName", ln);
    return contactMap;
  }
}
