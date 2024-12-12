package io.avaje.json.node;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonStringTest {

  static final JsonString jsonString = JsonString.of("foo");

  @Test
  void type() {
    assertThat(jsonString.type()).isEqualTo(JsonNode.Type.STRING);
    assertThat(jsonString.type().isValue()).isTrue();
    assertThat(jsonString.type().isNumber()).isFalse();
  }

  @Test
  void copy() {
    assertThat(jsonString.copy()).isSameAs(jsonString);
  }

  @Test
  void unmodifiable() {
    assertThat(jsonString.unmodifiable()).isSameAs(jsonString);
  }

  @Test
  void value() {
    assertThat(jsonString.value()).isEqualTo("foo");
    assertThat(jsonString.text()).isEqualTo("foo");
  }

}
