package org.example.customer.naming;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class PropertyUnderscoreTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<PropertyUnderscore> jsonType = jsonb.type(PropertyUnderscore.class);

  @Test
  void upperUnderscore_toFrom() {
    var bean = new PropertyUnderscore("sim", "simPlus");
    bean.setSetter("set");

    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"name\":\"sim\",\"email\":\"simPlus\",\"setter\":\"set\"}");

    var fromJson = jsonType.fromJson(asJson);
    assertEquals(fromJson, bean);
  }
}
