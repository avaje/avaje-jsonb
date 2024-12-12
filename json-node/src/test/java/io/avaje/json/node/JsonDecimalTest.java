package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JsonDecimalTest {

  static final JsonDecimal jsonDecimal = JsonDecimal.of(new BigDecimal("42.3"));

  @Test
  void type() {
    assertThat(jsonDecimal.type()).isEqualTo(JsonNode.Type.NUMBER);
    assertThat(jsonDecimal.type().isNumber()).isTrue();
  }

  @Test
  void isEqualTo() {
    assertThat(jsonDecimal).isEqualTo(JsonDecimal.of(new BigDecimal("42.3")));
    assertThat(jsonDecimal).isNotEqualTo(JsonDecimal.of(new BigDecimal("42.0")));
    assertThat(jsonDecimal).isNotEqualTo(JsonBoolean.of(false));
  }

  @Test
  void copy() {
    assertThat(jsonDecimal.copy()).isSameAs(jsonDecimal);
  }

  @Test
  void unmodifiable() {
    assertThat(jsonDecimal.unmodifiable()).isSameAs(jsonDecimal);
  }

  @Test
  void toPlain() {
    assertThat(jsonDecimal.toPlain()).isEqualTo(new BigDecimal("42.3"));
  }

  @Test
  void value() {
    assertThat(jsonDecimal.doubleValue()).isEqualTo(42.3D);
    assertThat(jsonDecimal.numberValue()).isEqualTo(new BigDecimal("42.3"));
    assertThat(jsonDecimal.decimalValue()).isEqualByComparingTo(new BigDecimal("42.3"));
  }

}
