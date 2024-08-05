package org.example.other.custom.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class TestSelectiveSerializer {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<CustomExample> jsonType = jsonb.type(CustomExample.class);

  @Test
  void toFromJson() {
    final var bean = new CustomExample(new BigDecimal("100.95630"), new BigDecimal("100.95630"));

    final String asJson = jsonType.toJson(bean);
    assertThat(asJson).isEqualTo("{\"amountOwed\":100.95,\"somethingElse\":100.95630}");

    final var fromJson = jsonType.fromJson(asJson);
    assertThat(fromJson.amountOwed()).isEqualTo(new BigDecimal("100.95"));
    assertThat(fromJson.somethingElse()).isEqualTo(new BigDecimal("100.95630"));
    assertThat(fromJson).isNotEqualTo(bean);
  }
}
