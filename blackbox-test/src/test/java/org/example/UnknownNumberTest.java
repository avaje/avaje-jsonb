package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

class UnknownNumberTest {

  Jsonb jsonb = Jsonb.builder().build();

  JsonType<List<Map<String, Object>>> listMapType =
      jsonb.type(Types.listOf(Types.mapOf(Object.class)));

  @Test
  void testIntegerParsing() {

    String input = "[ {\"food\": \"sushi\", \"amount\": 5}]";

    List<Map<String, Object>> l = listMapType.fromJson(input);
    assertThat("5").isEqualTo(l.get(0).get("amount").toString());
    assertThat("sushi").isEqualTo(l.get(0).get("amount").toString());
  }
}
