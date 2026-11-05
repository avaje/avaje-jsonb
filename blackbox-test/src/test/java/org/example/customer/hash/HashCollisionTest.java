package org.example.customer.hash;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HashCollisionTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<HashCollision> jsonType = jsonb.type(HashCollision.class);

  @Test
  void toFromJson() {
    var bean = new HashCollision("a", "b", "c");
    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"glbvs\":\"a\",\"yacxa\":\"b\",\"other\":\"c\"}");

    assertThat(jsonType.fromJson(asJson)).isEqualTo(bean);
    assertThat(jsonType.fromJson(new ByteArrayInputStream(asJson.getBytes(StandardCharsets.UTF_8)))).isEqualTo(bean);
  }

  @Test
  void fromJson_reverseOrder() {
    var bean = jsonType.fromJson("{\"other\":\"c\",\"yacxa\":\"b\",\"glbvs\":\"a\"}");
    assertThat(bean).isEqualTo(new HashCollision("a", "b", "c"));
  }
}
