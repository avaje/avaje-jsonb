package org.example.customer.node;

import io.avaje.json.node.JsonObject;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloMixedTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<HelloMixed> jsonType = jsonb.type(HelloMixed.class);

  @Test
  void test() {
    HelloMixed mixed = new HelloMixed("hi", JsonObject.create().add("a", "b"));

    String asJson = jsonType.toJson(mixed);
    assertThat(asJson).isEqualTo("{\"name\":\"hi\",\"other\":{\"a\":\"b\"}}");

    HelloMixed fromJson = jsonType.fromJson(asJson);

    assertThat(fromJson).isEqualTo(mixed);
  }
}
