package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JsonDoubleTest {

  static final JsonDouble jsonDouble = JsonDouble.of(42.3);

  @Test
  void type() {
    assertThat(jsonDouble.type()).isEqualTo(JsonNode.Type.NUMBER);
    assertThat(jsonDouble.type().isNumber()).isTrue();
  }

  @Test
  void isEqualTo() {
    assertThat(jsonDouble).isEqualTo(JsonDouble.of(42.3));
    assertThat(jsonDouble).isNotEqualTo(JsonDouble.of(42.2));
    assertThat(jsonDouble).isNotEqualTo(JsonBoolean.of(false));
  }

  @Test
  void copy() {
    assertThat(jsonDouble.copy()).isSameAs(jsonDouble);
  }

  @Test
  void unmodifiable() {
    assertThat(jsonDouble.unmodifiable()).isSameAs(jsonDouble);
  }

  @Test
  void toPlain() {
    assertThat(jsonDouble.toPlain()).isEqualTo(42.3D);
  }

  @Test
  void value() {
    assertThat(jsonDouble.intValue()).isEqualTo(42);
    assertThat(jsonDouble.longValue()).isEqualTo(42L);
    assertThat(jsonDouble.doubleValue()).isEqualTo(42.3D);
    assertThat(jsonDouble.numberValue()).isEqualTo(42.3D);
    assertThat(jsonDouble.decimalValue()).isEqualByComparingTo(new BigDecimal("42.3"));
  }

}
