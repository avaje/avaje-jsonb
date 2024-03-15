package org.example.customer.optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class OptionalsAccessTest {

  final String jsonStr = "{\"stringy\":\"StringyMcStringFace\"}";

  Jsonb jsonb = Jsonb.builder().serializeEmpty(false).build();

  @Test
  void anyToJson() {
    final var optionals = new OptionalAccess("StringyMcStringFace");

    final String asJson = jsonb.toJson(optionals);
    assertThat(asJson).isEqualTo(jsonStr);
  }

  @Test
  void nullJson() {
    final var optionals = new OptionalAccess(null);

    final var optionalsType = jsonb.type(OptionalAccess.class);
    final String asJson = optionalsType.toJson(optionals);
    assertThat(asJson).isEqualTo("{}");

    final OptionalAccess from2 = optionalsType.fromJson(asJson);
    assertThat(from2).isEqualTo(optionals);
  }

  @Test
  void toFromJson() {

    final var optionals = new OptionalAccess("StringyMcStringFace");
    final var optionalsType = jsonb.type(OptionalAccess.class);
    final String asJson = optionalsType.toJson(optionals);
    assertThat(asJson).isEqualTo(jsonStr);

    final OptionalAccess from2 = optionalsType.fromJson(asJson);
    assertThat(from2).isEqualTo(optionals);
  }

  @Test
  void toFromJson_viaTypeObject() {

    final Object optionalsAsObject = new OptionalAccess("StringyMcStringFace");

    final String asJson = jsonb.type(Object.class).toJson(optionalsAsObject);
    assertThat(asJson).isEqualTo(jsonStr);

    final OptionalAccess from1 = jsonb.type(OptionalAccess.class).fromJson(asJson);
    assertThat(from1).isEqualTo(optionalsAsObject);
  }
}
