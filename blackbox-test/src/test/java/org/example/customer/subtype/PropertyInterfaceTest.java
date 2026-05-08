package org.example.customer.subtype;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyInterfaceTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<PropertyInterface> type = jsonb.type(PropertyInterface.class);

  @Test
  void toJson_includesMethodOnlyProperty() {
    PropertyInterface.NestedTest nested = new PropertyInterface.NestedTest();
    String asJson = type.toJson(nested);
    assertThat(asJson).isEqualTo("{\"@type\":\"PropertyInterface.NestedTest\",\"methodOnly\":\"foo\"}");
  }

  @Test
  void fromJson_roundTrip() {
    String json = "{\"@type\":\"PropertyInterface.NestedTest\",\"methodOnly\":\"foo\"}";
    PropertyInterface result = type.fromJson(json);
    assertThat(result).isInstanceOf(PropertyInterface.NestedTest.class);
    assertThat(result.methodOnly()).isEqualTo("foo");
  }
}
