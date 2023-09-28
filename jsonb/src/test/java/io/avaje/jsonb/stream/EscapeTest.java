package io.avaje.jsonb.stream;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class EscapeTest {

  @Test
  void nameHash_withQuotes() {
    assertThat(Escape.nameHash("\"with quotes\"")).isEqualTo(523918728);
    assertThat(Escape.nameHash("\"withquotes\"")).isEqualTo(420906694);
    assertThat(Escape.nameHash("withquotes")).isEqualTo(-1426871072);
  }

  @Test
  void nameHash_unquoted() {
    assertThat(Escape.nameHash("foo")).isEqualTo(-1443660073);
    assertThat(Escape.nameHash("fop")).isEqualTo(-1359771978);
    assertThat(Escape.nameHash("bar")).isEqualTo(1991736602);
  }

  @Test
  void hello() {
    assertThat(asString(Escape.quoteEscape("Hello"))).isEqualTo("\"Hello\"");
  }

  @Test
  void quote() {
    assertThat(asString(Escape.quoteEscape("H\"ello"))).isEqualTo("\"H\\\"ello\"");
  }

  @Test
  void escape() {
    assertThat(asString(Escape.quoteEscape("a\\z"))).isEqualTo("\"a\\\\z\"");
    assertThat(asString(Escape.quoteEscape("a\nz"))).isEqualTo("\"a\\nz\"");
    assertThat(asString(Escape.quoteEscape("a\tz"))).isEqualTo("\"a\\tz\"");
    assertThat(asString(Escape.quoteEscape("a\bz"))).isEqualTo("\"a\\bz\"");
  }

  private String asString(byte[] helloBytes) {
    return new String(helloBytes, 0, helloBytes.length, StandardCharsets.UTF_8);
  }
}
