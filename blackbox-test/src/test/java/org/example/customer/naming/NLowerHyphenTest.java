package org.example.customer.naming;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NLowerHyphenTest {

  Jsonb jsonb = Jsonb.newBuilder().build();
  JsonType<NLowHyp> jsonType = jsonb.type(NLowHyp.class);

  @Test
  void upperUnderscore_toFrom() throws IOException {
    NLowHyp bean = new NLowHyp("sim", "simPlus", 42);

    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"simple\":\"sim\",\"simple-plus\":\"simPlus\",\"my-one-red\":42}");

    NLowHyp fromJson = jsonType.fromJson(asJson);
    assertEquals(fromJson, bean);
  }
}
