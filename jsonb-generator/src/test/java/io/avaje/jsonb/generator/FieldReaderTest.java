package io.avaje.jsonb.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldReaderTest {

  @Test
  void trimAnnotations() {
    assertEquals("java.lang.String", Util.trimAnnotations("java.lang.String"));
    assertEquals("java.lang.String", Util.trimAnnotations("java.lang.@javax.validation.constraints.Email String"));
    assertEquals("java.lang.String", Util.trimAnnotations("java.lang.@javax.validation.constraints.NotNull,@javax.validation.constraints.Size(min=2, max=150) String"));
    assertEquals("java.lang.String", Util.trimAnnotations("java.lang.@javax.validation.constraints.Email,@javax.validation.constraints.Size(max=100) String"));
  }
}
