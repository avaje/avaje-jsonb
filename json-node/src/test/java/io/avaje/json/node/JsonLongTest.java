package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JsonLongTest {

  static final JsonLong jsonLong = JsonLong.of(42);

  @Test
  void type() {
    assertThat(jsonLong.type()).isEqualTo(JsonNode.Type.NUMBER);
    assertThat(jsonLong.type().isNumber()).isTrue();
  }

  @Test
  void copy() {
    assertThat(jsonLong.copy()).isSameAs(jsonLong);
  }

  @Test
  void unmodifiable() {
    assertThat(jsonLong.unmodifiable()).isSameAs(jsonLong);
  }

  @Test
  void value() {
    assertThat(jsonLong.intValue()).isEqualTo(42);
    assertThat(jsonLong.longValue()).isEqualTo(42L);
    assertThat(jsonLong.doubleValue()).isEqualTo(42D);
    assertThat(jsonLong.numberValue()).isEqualTo(42L);
    assertThat(jsonLong.decimalValue()).isEqualTo(BigDecimal.valueOf(42));
  }

}
