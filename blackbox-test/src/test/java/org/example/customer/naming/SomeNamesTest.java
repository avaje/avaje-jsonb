package org.example.customer.naming;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SomeNamesTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<SomeNames> jsonType = jsonb.type(SomeNames.class);

  @Test
  void toFrom()  {
    SomeNames bean = new SomeNames("a", "b","c","d", "e", "f");

    String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"$a\":\"a\",\"$b\":\"b\",\"#\":\"c\",\"$\":\"d\",\"$foo\":\"e\",\"\\\"with quotes\\\"\":\"f\"}");

    SomeNames fromJson = jsonType.fromJson(asJson);
    assertEquals(fromJson, bean);
  }
}
