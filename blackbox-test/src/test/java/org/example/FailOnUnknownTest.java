package org.example;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.example.customer.Customer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailOnUnknownTest {

  @Test
  void fromJson_default_ignoresUnknown() throws IOException {
    String jsonContent = "{\"unknown\":\"foo\",\"unknownObject\":{\"a\":\"aval\"},\"unknownList\":[5,6,7],\"id\":42,\"name\":\"hello\"}";

    // default skips unknown
    Jsonb jsonb = Jsonb.newBuilder().build();
    JsonType<Customer> jsonType = jsonb.type(Customer.class);

    Customer customer = jsonType.fromJson(jsonContent);

    // mySecret1 and mySecret2 are not deserialized due to @Json.Ignore
    assertThat(customer.id()).isEqualTo(42L);
    assertThat(customer.name()).isEqualTo("hello");
  }

  @Test
  void fromJson_with_failOnUnknownTrue() {
    // failOnUnknown set to true
    Jsonb jsonb = Jsonb.newBuilder().failOnUnknown(true).build();
    JsonType<Customer> jsonType = jsonb.type(Customer.class);

    String jsonContent0 = "{\"unknownScalar\":\"foo\",\"unknownObject\":{\"a\":\"aval\"},\"unknownList\":[5,6,7],\"id\":42,\"name\":\"hello\"}";
    assertThatThrownBy(() -> jsonType.fromJson(jsonContent0)).hasMessageContaining("Unknown property unknownScalar");

    String jsonContent1 = "{\"name\":\"hello\",\"unknownObject\":{\"a\":\"aval\"},\"unknownList\":[5,6,7],\"id\":42}";
    assertThatThrownBy(() -> jsonType.fromJson(jsonContent1)).hasMessageContaining("Unknown property unknownObject");

    String jsonContent2 = "{\"name\":\"hello\",\"unknownList\":[5,6,7],\"id\":42}";
    assertThatThrownBy(() -> jsonType.fromJson(jsonContent2)).hasMessageContaining("Unknown property unknownList");
  }
}
