package org.example.customer.naming;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NLowerUnderscoreTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<NLowUnder> jsonType = jsonb.type(NLowUnder.class);

  @Test
  void upperUnderscore_toFrom()  {
    NLowUnder bean = new NLowUnder("sim", "simPlus", 42);

    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"simple\":\"sim\",\"simple_plus\":\"simPlus\",\"my_one_red\":42}");

    NLowUnder fromJson = jsonType.fromJson(asJson);
    assertEquals(fromJson, bean);
  }
}
