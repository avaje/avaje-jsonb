package org.example.other.custom;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class CustomClassJsonAdapterTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<CustomClass> jsonType = jsonb.type(CustomClass.class);

  @Test
  void toFromJson() {
    final var bean = new CustomClass("link");

    final String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"body\":\"link\"}");

    final var fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson.body()).isEqualTo(bean.body());
    assertThat(fromJson).isEqualTo(bean);
  }
}
