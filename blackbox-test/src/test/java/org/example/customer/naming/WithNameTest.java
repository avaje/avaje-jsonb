package org.example.customer.naming;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WithNameTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<WithName> jsonType = jsonb.type(WithName.class);

  @Test
  void toFrom()  {
    WithName bean = new WithName("sim", "simPlus", 42);

    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"Some Thing Odd\":\"sim\",\"simple-plus\":\"simPlus\",\"my-one-red\":42,\"derived\":84}");

    WithName fromJson = jsonType.fromJson(asJson);
    assertEquals(fromJson, bean);
  }
}
