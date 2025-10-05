package org.cascade.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.cascade.example.OtherJarJsonbCascade;
import org.cascade.example.UnCascadable;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class TestCascade {
  static Jsonb jsonb = Jsonb.instance();

  @Test
  void testOtherJarCascade() {
    assertDoesNotThrow(() -> jsonb.type(OtherJarJsonbCascade.class));
  }

  @Test
  void failedCascade() {
    assertThrows(IllegalArgumentException.class, () -> jsonb.type(UnCascadable.class));
  }
}
