package org.example.customer.naming;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NUpperUnderscoreTest {

  Jsonb jsonb = Jsonb.newBuilder().build();
  JsonType<NUppUnder> jsonType = jsonb.type(NUppUnder.class);

  @Test
  void upperUnderscore_toFrom() throws IOException {
    NUppUnder bean = new NUppUnder("sim", "simPlus", 42);

    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"SIMPLE\":\"sim\",\"SIMPLE_PLUS\":\"simPlus\",\"MY_ONE_RED\":42}");

    NUppUnder fromJson = jsonType.fromJson(asJson);
    assertEquals(fromJson, bean);
  }
}
