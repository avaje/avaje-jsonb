package org.example.customer;

import io.avaje.json.*;
import io.avaje.jsonb.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

  final String jsonStart = "{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\",\"whenCreated\":";

  Jsonb jsonb = Jsonb.builder().serializeEmpty(false).build();

  @Test
  void anyToJson() {
    var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);
    String asJson = jsonb.toJson(customer);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\"}");
  }

  @Test
  void anyToJsonPretty() {
    final var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);
    final String asJson = jsonb.toJsonPretty(customer).replace("\" : ", "\": ");
    assertThat(asJson).isEqualTo("""
   {
     "id": 42,
     "name": "rob",
     "status": "ACTIVE"
   }""");
  }

  @Test
  void anyToWriter() {
    var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);
    StringWriter writer = new StringWriter();
    jsonb.toJson(customer,  writer);
    assertThat(writer.toString()).isEqualTo("{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\"}");
  }

  @Test
  void anyToOutputStream() {
    var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    jsonb.toJson(customer,  baos);
    String asString = baos.toString(StandardCharsets.UTF_8);
    assertThat(asString).isEqualTo("{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\"}");
  }

  @Test
  void anyToBytes() {
    var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);
    byte[] bytes = jsonb.toJsonBytes(customer);
    String asString = new String(bytes, StandardCharsets.UTF_8);
    assertThat(asString).isEqualTo("{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\"}");
  }

  @Test
  void toJson_view() {
    JsonView<Customer> customerJsonView = jsonb.type(Customer.class).view("(id, name)");

    var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE).whenCreated(Instant.now());

    String asJson = customerJsonView.toJson(customer);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\"}");
  }

  @Test
  void toJson()  {

    Customer customer = customer();

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

  private Customer customer() {
    var customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE).whenCreated(Instant.now());
    customer.contacts().add(new Contact(UUID.randomUUID(), "fo", "nar"));
    customer.contacts().add(new Contact(UUID.randomUUID(), "ba", "zar"));
    return customer;
  }

  @Test
  void toJson_writer() {

    Customer customer = customer();

    JsonType<Customer> customerType = jsonb.type(Customer.class);
    StringWriter sw = new StringWriter();

    customerType.toJson(customer, sw);
    assertThat(sw.toString()).startsWith(jsonStart);
  }

  @Test
  void fromObject()  {

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

  @Test
  void toJson_viaTypeObject()  {

    Object customerAsObject = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);

    String asJson = jsonb.type(Object.class).toJson(customerAsObject);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"status\":\"ACTIVE\"}");

    Customer from1 = jsonb.type(Customer.class).fromJson(asJson);
    assertThat(from1.id()).isEqualTo(42L);
    assertThat(from1.name()).isEqualTo("rob");
    assertThat(from1.status()).isEqualTo(Customer.Status.ACTIVE);
  }

  @Test
  void toJson_pretty() {
    StringWriter stringWriter = new StringWriter();
    try (JsonWriter writer = jsonb.writer(stringWriter)) {
      writer.pretty(true);
      Customer customer = new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE);
      jsonb.type(Customer.class).toJson(customer, writer);
    }
    final String prettyJson = stringWriter.toString().replace("\" : ", "\": ");
    assertThat(prettyJson)
        .isEqualTo("""
       {
         "id": 42,
         "name": "rob",
         "status": "ACTIVE"
       }""");
  }

  @Test
  void toJsonStream() {

    var customers = List.of(
      new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE),
      new Customer().id(43L).name("job").status(Customer.Status.NEW),
      new Customer().id(44L).name("bob").status(Customer.Status.ACTIVE));

    JsonType<Customer> type = jsonb.type(Customer.class);
    StringWriter writer = new StringWriter();

    try (JsonWriter jsonWriter = jsonb.writer(writer)) {
      jsonWriter.pretty(false);
      for (Customer customer : customers) {
        type.toJson(customer, jsonWriter);
        jsonWriter.writeNewLine();
      }
    }
    String streamJson = writer.toString().replace(" ", "");
    assertThat(streamJson).isEqualTo(
    	      """
    	  	{"id":42,"name":"rob","status":"ACTIVE"}
    	  	{"id":43,"name":"job","status":"NEW"}
    	  	{"id":44,"name":"bob","status":"ACTIVE"}
    	  	""");
  }

  @Test
  void toJsonStream_viaMethod() {
    var customers = List.of(
      new Customer().id(42L).name("rob").status(Customer.Status.ACTIVE),
      new Customer().id(43L).name("job").status(Customer.Status.NEW),
      new Customer().id(44L).name("bob").status(Customer.Status.ACTIVE));

    StringWriter writer = new StringWriter();
    try (JsonWriter jsonWriter = jsonb.writer(writer)) {
      toStream(customers.iterator(), jsonWriter);
    }

    String streamJson = writer.toString().replace(" ", "");
    assertThat(streamJson).isEqualTo(
      """
  	{"id":42,"name":"rob","status":"ACTIVE"}
  	{"id":43,"name":"job","status":"NEW"}
  	{"id":44,"name":"bob","status":"ACTIVE"}
  	""");
  }

  <T> void toStream(Iterator<T> iterator, JsonWriter jsonWriter) {
    if (iterator.hasNext()) {
      jsonWriter.pretty(false);
      T first = iterator.next();
      JsonType<T> type = jsonb.typeOf(first);
      type.toJson(first, jsonWriter);
      jsonWriter.writeNewLine();
      while (iterator.hasNext()) {
        type.toJson(iterator.next(), jsonWriter);
        jsonWriter.writeNewLine();
      }
    }
  }
}
