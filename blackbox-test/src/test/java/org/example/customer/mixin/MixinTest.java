package org.example.customer.mixin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class MixinTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    final var bean = new CrewMate("red", 999);

    final var asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"color\":\"red\"}");

    final var fromJson = jsonb.type(CrewMate.class).fromJson("{\"color\":\"blue\",\"susLv\":\"0\"}");
    assertThat(fromJson.getC()).isEqualTo("blue");
    assertThat(fromJson.getSusLv()).isZero();
  }
}
