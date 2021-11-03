package io.avaje.jsonb.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

  @Test
  void initcap() {
    assertEquals("Hello", Util.initcap("hello"));
    assertEquals("Url", Util.initcap("url"));
    assertEquals("Fo", Util.initcap("fo"));
    assertEquals("A", Util.initcap("a"));
  }
}
