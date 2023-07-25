package io.avaje.jsonb.generator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilTest {

  @Test
  void initCap() {
    assertEquals("Hello", Util.initCap("hello"));
    assertEquals("Url", Util.initCap("url"));
    assertEquals("Fo", Util.initCap("fo"));
    assertEquals("A", Util.initCap("a"));
    assertEquals("InitCap", Util.initCap("initCap"));
  }

  @Test
  void initLower() {
    assertEquals("hello", Util.initLower("hello"));
    assertEquals("url", Util.initLower("URL"));
    assertEquals("initCap", Util.initLower("InitCap"));
  }

  @Test
  void escapeQuotes() {
    assertEquals("\\\"Hi\\\"", Util.escapeQuotes("\"Hi\""));
    assertEquals("\\\"\\\"", Util.escapeQuotes("\"\""));

    assertEquals("\\\"Hi", Util.escapeQuotes("\"Hi"));
    assertEquals("Hi\\\"", Util.escapeQuotes("Hi\""));
  }

  @Test
  void escapeQuotesList() {
    assertThat(Util.escapeQuotes(List.of("a", "b"))).containsOnly("a", "b");
    assertThat(Util.escapeQuotes(List.of("\"a\""))).containsOnly("\\\"a\\\"");
    assertThat(Util.escapeQuotes(List.of("\"a\"", "\"b\""))).containsOnly("\\\"a\\\"", "\\\"b\\\"");

    assertThat(Util.escapeQuotes(List.of("\"a\"", "b"))).containsOnly("\\\"a\\\"", "b");
    assertThat(Util.escapeQuotes(List.of("a", "\"b\""))).containsOnly("a", "\\\"b\\\"");
  }
}
