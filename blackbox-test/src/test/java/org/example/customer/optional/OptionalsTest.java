package org.example.customer.optional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class OptionalsTest {

  final String jsonStr =
      "{\"stringyString\":\"StringyMcStringFace\",\"intOp\":21,\"doubleOp\":6.9,\"longOp\":420}";

  Jsonb jsonb = Jsonb.builder().serializeEmpty(false).build();
  Jsonb jsonbNull = Jsonb.builder().serializeNulls(true).build();

  @Test
  void anyToJson() {
    final var optionals = optionals();

    final String asJson = jsonb.toJson(optionals);
    assertThat(asJson).isEqualTo(jsonStr);
  }

  @Test
  void emptyJson() {
    final var optionals = new Optionals();

    final var optionalsType = jsonb.type(Optionals.class);
    final String asJson = optionalsType.toJson(optionals);
    assertThat(asJson).isEqualTo("{}");

    final Optionals from2 = optionalsType.fromJson(asJson);
    assertThat(from2).isEqualTo(optionals);
  }

  @Test
  void nullJson() {
    final var optionals = new Optionals();

    final var optionalsType = jsonbNull.type(Optionals.class);
    final String asJson = optionalsType.toJson(optionals);
    assertThat(asJson).isEqualTo("{\"stringyString\":null,\"intOp\":null,\"doubleOp\":null,\"longOp\":null,\"publicOp\":null}");

    final Optionals from2 = optionalsType.fromJson(asJson);
    assertThat(from2).isEqualTo(optionals);
  }

  @Test
  void toFromJson() {

    final var optionals = optionals();
    final var optionalsType = jsonb.type(Optionals.class);
    final String asJson = optionalsType.toJson(optionals);
    assertThat(asJson).isEqualTo(jsonStr);

    final Optionals from2 = optionalsType.fromJson(asJson);
    assertThat(from2).isEqualTo(optionals);
  }

  private @NonNull Optionals optionals() {
    final var optionals = new Optionals();

    optionals.setStringyString("StringyMcStringFace");
    optionals.setIntOp(21);
    optionals.setDoubleOp(6.9);
    optionals.setLongOp(420);
    optionals.publicOp = Optional.empty();
    return optionals;
  }

  @Test
  void toFromJson_viaTypeObject() {

    final Object optionalsAsObject = optionals();

    final String asJson = jsonb.type(Object.class).toJson(optionalsAsObject);
    assertThat(asJson).isEqualTo(jsonStr);

    final Optionals from1 = jsonb.type(Optionals.class).fromJson(asJson);
    assertThat(from1).isEqualTo(optionalsAsObject);
  }
}
