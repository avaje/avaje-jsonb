package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JsonIntegerTest {

  static final JsonInteger jsonInteger = JsonInteger.of(42);

  @Test
  void type() {
    assertThat(jsonInteger.type()).isEqualTo(JsonNode.Type.NUMBER);
    assertThat(jsonInteger.type().isNumber()).isTrue();
  }

  @Test
  void isEqualTo() {
    assertThat(jsonInteger).isEqualTo(JsonInteger.of(42));
    assertThat(jsonInteger).isNotEqualTo(JsonInteger.of(43));
    assertThat(jsonInteger).isNotEqualTo(JsonBoolean.of(false));
  }

  @Test
  void copy() {
    assertThat(jsonInteger.copy()).isSameAs(jsonInteger);
  }

  @Test
  void unmodifiable() {
    assertThat(jsonInteger.unmodifiable()).isSameAs(jsonInteger);
  }

  @Test
  void toPlain() {
    assertThat(jsonInteger.toPlain()).isEqualTo(42);
  }

  @Test
  void value() {
    assertThat(jsonInteger.intValue()).isEqualTo(42);
    assertThat(jsonInteger.longValue()).isEqualTo(42L);
    assertThat(jsonInteger.doubleValue()).isEqualTo(42D);
    assertThat(jsonInteger.numberValue()).isEqualTo(42);
    assertThat(jsonInteger.decimalValue()).isEqualTo(BigDecimal.valueOf(42));
  }

}
