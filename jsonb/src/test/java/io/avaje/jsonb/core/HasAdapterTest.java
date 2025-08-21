package io.avaje.jsonb.core;

import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class HasAdapterTest {

  private final Jsonb jsonb = Jsonb.builder().build();

  @Test
  @DisplayName("hasAdapter returns true for basic types and primitives")
  void hasAdapter_basicTypes_returnsTrue() {
    assertThat(jsonb.hasAdapter(String.class)).isTrue();
    assertThat(jsonb.hasAdapter(Integer.class)).isTrue();
    assertThat(jsonb.hasAdapter(Boolean.class)).isTrue();

    assertThat(jsonb.hasAdapter(int.class)).isTrue();
    assertThat(jsonb.hasAdapter(boolean.class)).isTrue();
    assertThat(jsonb.hasAdapter(double.class)).isTrue();
  }

  @Test
  @DisplayName("hasAdapter returns true for generic types like List, Map, and Optional")
  void hasAdapter_withGenericTypes_returnsTrue() {
    Type listOfString = Types.listOf(String.class);
    assertThat(jsonb.hasAdapter(listOfString)).isTrue();

    Type mapOfStringToInteger = Types.mapOf(Integer.class);
    assertThat(jsonb.hasAdapter(mapOfStringToInteger)).isTrue();

    Type optionalString = Types.newParameterizedType(Optional.class, String.class);
    assertThat(jsonb.hasAdapter(optionalString)).isTrue();
  }

  @Test
  @DisplayName("hasAdapter returns false for types without adapters")
  void hasAdapter_whenAdapterNotExists_returnsFalse() {
    assertThat(jsonb.hasAdapter(UnknownClass.class)).isFalse();
    assertThat(jsonb.hasAdapter(SomeInterface.class)).isFalse();
  }

  @Test
  @DisplayName("hasAdapter works correctly with cached adapters")
  void hasAdapter_withCachedAdapter_returnsTrue() {
    jsonb.type(String.class);
    assertThat(jsonb.hasAdapter(String.class)).isTrue();
  }

  @Test
  @DisplayName("hasAdapter never throws exceptions, even for problematic types")
  void hasAdapter_doesNotThrowExceptions() {
    assertThat(jsonb.hasAdapter(UnknownClass.class)).isFalse();
    assertThat(jsonb.hasAdapter(SomeInterface.class)).isFalse();
  }

  // Test classes
  private static class UnknownClass {
    private String value;
  }

  private interface SomeInterface {
    String getValue();
  }
}
