package org.example.customer.subtype;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InheritanceSplitPropertyTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<InheritanceSplitProperty> type = jsonb.type(InheritanceSplitProperty.class);

  @Test
  void toJson() {
    InheritanceSplitProperty.NestedTest nested = new InheritanceSplitProperty.NestedTest(42);
    String asJson = type.toJson(nested);
    assertThat(asJson)
        .isEqualTo("{\"@type\":\"InheritanceSplitPropertyTest.NestedTest\",\"value\":42}");
  }

  @Test
  void fromJson() {
    String json = "{\"@type\":\"InheritanceSplitPropertyTest.NestedTest\",\"value\":42}";
    InheritanceSplitProperty result = type.fromJson(json);
    assertThat(result).isInstanceOf(InheritanceSplitProperty.NestedTest.class);
    assertThat(result.value()).isEqualTo(42);
  }

  @Test
  void roundTrip() {
    InheritanceSplitProperty.NestedTest nested = new InheritanceSplitProperty.NestedTest(99);
    String asJson = type.toJson(nested);
    InheritanceSplitProperty result = type.fromJson(asJson);
    assertThat(result.value()).isEqualTo(99);
  }
}
