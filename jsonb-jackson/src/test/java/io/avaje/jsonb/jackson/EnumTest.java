package io.avaje.jsonb.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class EnumTest {

  enum Odd { ONE, CAFÉ }

  Jsonb jsonb = Jsonb.builder().adapter(new JacksonAdapter()).build();

  @Test
  void toJson_fromJson() {
    JsonType<List<Odd>> type = jsonb.type(Odd.class).list();
    String asJson = type.toJson(List.of(Odd.ONE, Odd.CAFÉ));
    assertThat(asJson).isEqualTo("[\"ONE\",\"CAFÉ\"]");
    assertThat(type.fromJson(asJson)).containsExactly(Odd.ONE, Odd.CAFÉ);
  }
}
