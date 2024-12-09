package org.example.customer.skip;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.json.stream.JsonStream;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyCustomerTest {

  @Test
  void toJson_fromJson() {

    Jsonb jsonb = Jsonb.builder().adapter(JsonStream.builder().build()).build();

    MyCustomer myCustomer = new MyCustomer(42, "rob", "foo");
    JsonType<MyCustomer> type = jsonb.type(MyCustomer.class);

    String asJson = type.toJson(myCustomer);
    assertThat(asJson).isEqualTo("{\"id\":42,\"name\":\"rob\",\"notes\":\"foo\"}");

    MyCustomer fromJson = type.fromJson(asJson);

    assertThat(fromJson.id()).isEqualTo(myCustomer.id());
    assertThat(fromJson.name()).isEqualTo(myCustomer.name());
    assertThat(fromJson.notes()).isEqualTo(myCustomer.notes());

    String asJson2 = type.toJson(myCustomer);
    assertThat(asJson2).isEqualTo("{\"id\":42,\"name\":\"rob\",\"notes\":\"foo\"}");

    MyCustomer fromJsonBytes = type.fromJson(asJson.getBytes(StandardCharsets.UTF_8));

    assertThat(fromJsonBytes.id()).isEqualTo(myCustomer.id());
    assertThat(fromJsonBytes.name()).isEqualTo(myCustomer.name());
    assertThat(fromJsonBytes.notes()).isEqualTo(myCustomer.notes());
  }

  @Test
  void list_toJson_fromJson() {

    Jsonb jsonb = Jsonb.builder().adapter(JsonStream.builder().build()).build();

    List<MyCustomer> customers = new ArrayList<>();
    customers.add(new MyCustomer(42, "rob", "foo"));
    customers.add(new MyCustomer(43, "bob", "bar"));

    JsonType<MyCustomer> type = jsonb.type(MyCustomer.class);

    String asJson = type.list().toJson(customers);
    assertThat(asJson).isEqualTo("[{\"id\":42,\"name\":\"rob\",\"notes\":\"foo\"},{\"id\":43,\"name\":\"bob\",\"notes\":\"bar\"}]");

    List<MyCustomer> fromJson = type.list()
      .fromJson(asJson);

    assertThat(fromJson).hasSize(2);
    MyCustomer first = fromJson.get(0);
    assertThat(first.id()).isEqualTo(42);
    assertThat(first.name()).isEqualTo("rob");
    assertThat(first.notes()).isEqualTo("foo");
  }
}
