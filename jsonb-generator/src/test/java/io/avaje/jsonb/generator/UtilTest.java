package io.avaje.jsonb.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonReader;

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
  void fieldNameHash_matchesRuntime() {
    for (String name : List.of("", "id", "firstName", "café", "日本", "😀", "$a", "#")) {
      assertEquals(JsonReader.nameHash(name), Util.fieldNameHash(name));
    }
    // generator names are java string literal escaped
    assertEquals(JsonReader.nameHash("\"with quotes\""), Util.fieldNameHash(Util.escapeQuotes("\"with quotes\"")));
    assertEquals(JsonReader.nameHash("a\\b"), Util.fieldNameHash("a\\\\b"));
  }

  @Test
  void constantName() {
    assertEquals("ID", Util.constantName("id"));
    assertEquals("FIRST_NAME", Util.constantName("firstName"));
    assertEquals("FIRST_NAME", Util.constantName("first-name"));
    assertEquals("FIRST_NAME", Util.constantName("first_name"));
    assertEquals("ADDRESS2", Util.constantName("address2"));
    assertEquals("URL", Util.constantName("URL"));
    assertEquals("F_1ST", Util.constantName("1st"));
    assertEquals("F__", Util.constantName("#"));
    assertEquals("_A", Util.constantName("$a"));
    assertEquals("F_", Util.constantName(""));
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

  @Test
  void sanitizeImports() {
    assertEquals("org.foo.Bar", Util.sanitizeImports("org.foo.Bar"));
    assertEquals("org.foo.Bar", Util.sanitizeImports("org.foo.Bar[]"));
    assertEquals("org.foo.Bar", Util.sanitizeImports("@some.Nullable org.foo.Bar[]"));
  }

  @Test
  void validImportType_expect_false() {
    assertFalse(Util.validImportType("int", "org.foo"));
    assertFalse(Util.validImportType("java.lang.Integer", "org.foo"));
    assertFalse(Util.validImportType("org.foo.Bar", "org.foo"));
  }

  @Test
  void validImportType_expect_true() {
    assertTrue(Util.validImportType("java.lang.something.Foo", "org.foo"));
    assertTrue(Util.validImportType("org.foo.some.Bar", "org.foo"));
    assertTrue(Util.validImportType("org.other.Bar", "org.foo"));
  }
}
