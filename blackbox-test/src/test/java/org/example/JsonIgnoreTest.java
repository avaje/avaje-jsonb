package org.example;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.example.customer.Customer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonIgnoreTest {

  Jsonb jsonb = Jsonb.builder().serializeEmpty(false).build();
  JsonType<Customer> jsonType = jsonb.type(Customer.class);

  @Test
  void toJson() {
    var customer = new Customer().id(42L).mySecret1("s1").mySecret2("s2").mySecret3("s3");

    String asJson = jsonType.toJson(customer);
    // mySecret1 and mySecret3 are not serialized due to @Json.Ignore
    assertThat(asJson).isEqualTo("{\"id\":42,\"mySecret2\":\"s2\"}");
  }

  @Test
  void fromJson() {

    String jsonContent = "{\"id\":42,\"mySecret1\":\"s1\",\"mySecret2\":\"s2\",\"mySecret3\":\"s3\"}";

    Customer customer = jsonType.fromJson(jsonContent);

    // mySecret1 and mySecret2 are not deserialized due to @Json.Ignore
    assertThat(customer.getMySecret1()).isNull();
    assertThat(customer.getMySecret2()).isNull();
    assertThat(customer.getMySecret3()).isEqualTo("s3");
  }
}
