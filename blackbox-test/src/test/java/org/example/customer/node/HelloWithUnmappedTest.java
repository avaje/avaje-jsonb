package org.example.customer.node;

import io.avaje.json.node.JsonObject;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWithUnmappedTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<HelloWithUnmapped> jsonType = jsonb.type(HelloWithUnmapped.class);

  @Test
  void test() {
    var source = new HelloWithUnmapped("hi", 3, JsonObject.create().add("extra", "b").add("extra2", 54L));

    String asJson = jsonType.toJson(source);
    assertThat(asJson).isEqualTo("{\"name\":\"hi\",\"count\":3,\"extra\":\"b\",\"extra2\":54}");

    HelloWithUnmapped fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson).isEqualTo(source);
  }
}
