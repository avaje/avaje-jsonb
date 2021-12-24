package io.avaje.jsonb.diesel;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class EscapeTest {

  @Test
  void hello() {
    assertThat(asString(Escape.quoteEscape("Hello"))).isEqualTo("Hello");
  }

  @Test
  void quote() {
    assertThat(asString(Escape.quoteEscape("H\"ello"))).isEqualTo("H\"ello");
  }

  @Test
  void escape() {
    assertThat(asString(Escape.quoteEscape("a\\b"))).isEqualTo("a\\\\b");
    assertThat(asString(Escape.quoteEscape("a\nb"))).isEqualTo("a\\nb");
    assertThat(asString(Escape.quoteEscape("a\tb"))).isEqualTo("a\\tb");
    assertThat(asString(Escape.quoteEscape("a\bb"))).isEqualTo("a\\bb");
  }

  private String asString(byte[] helloBytes) {
    return new String(helloBytes, 0, helloBytes.length, StandardCharsets.UTF_8);
  }
}
