package io.avaje.jsonb.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

  @Test
  void baseTypeOfAdapter() {
    assertEquals("com.My", Util.baseTypeOfAdapter("com.jsonb.MyJsonAdapter"));
    assertEquals("com.foo.My", Util.baseTypeOfAdapter("com.foo.jsonb.MyJsonAdapter"));
    assertEquals("com.foo.bar.SomeType", Util.baseTypeOfAdapter("com.foo.bar.jsonb.SomeTypeJsonAdapter"));
    assertEquals("SomeType", Util.baseTypeOfAdapter("jsonb.SomeTypeJsonAdapter"));
  }

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
}
