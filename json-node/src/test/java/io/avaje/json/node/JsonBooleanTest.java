package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class JsonBooleanTest {

  static final JsonBoolean jsonBoolean = JsonBoolean.of(true);

  @Test
  void type() {
    assertThat(jsonBoolean.type()).isEqualTo(JsonNode.Type.BOOLEAN);
    assertThat(jsonBoolean.type().isValue()).isTrue();
    assertThat(jsonBoolean.type().isNumber()).isFalse();
  }

  @Test
  void isEqualTo() {
    assertThat(jsonBoolean).isEqualTo(JsonBoolean.of(true));
    assertThat(jsonBoolean).isNotEqualTo(JsonBoolean.of(false));
  }

  @Test
  void copy() {
    assertThat(jsonBoolean.copy()).isSameAs(jsonBoolean);
  }

  @Test
  void toPlain() {
    assertThat(jsonBoolean.toPlain()).isTrue();
    assertThat(JsonBoolean.of(false).toPlain()).isFalse();
  }

  @Test
  void unmodifiable() {
    assertThat(jsonBoolean.unmodifiable()).isSameAs(jsonBoolean);
  }

  @Test
  void value() {
    assertThat(jsonBoolean.value()).isTrue();
    assertThat(JsonBoolean.of(false).value()).isFalse();
  }

}
