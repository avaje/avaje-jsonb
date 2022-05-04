package org.example.customer.naming;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NLowerHyphenTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<NLowHyp> jsonType = jsonb.type(NLowHyp.class);

  @Test
  void upperUnderscore_toFrom()  {
    NLowHyp bean = new NLowHyp("sim", "simPlus", 42);

    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"simple\":\"sim\",\"simple-plus\":\"simPlus\",\"my-one-red\":42}");

    NLowHyp fromJson = jsonType.fromJson(asJson);
    assertEquals(fromJson, bean);
  }

  @Test
  void mapOfRecord()  {

    JsonType<Map<String, NLowHyp>> mapJsonType = jsonType.map();

    Map<String, NLowHyp> map = new LinkedHashMap<>();
    map.put("aaa",new NLowHyp("sim", "simPlus", 42));
    map.put("bbb",new NLowHyp("bSim", "bSimPlus", 43));

    String asJson = mapJsonType.toJson(map);

    Map<String, NLowHyp> fromJson = mapJsonType.fromJson(asJson);
    assertThat(fromJson).isEqualTo(map);

  }
}
