package org.example.customer.mixin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.json.JsonException;
import io.avaje.jsonb.Jsonb;

class EnumMixinTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void mixInToJson() {
    final var type = jsonb.type(Tool.class);
    assertThat(type.toJson(Tool.HAMMER)).isEqualTo("\"hammer\"");
    assertThat(type.toJson(Tool.WRENCH)).isEqualTo("\"WRENCH\"");
    assertThatThrownBy(() -> type.toJson(Tool.SECRET)).isInstanceOf(JsonException.class);
    assertThatThrownBy(() -> type.toJson(Tool.LEGACY)).isInstanceOf(JsonException.class);
  }

  @Test
  void mixInFromJson() {
    final var type = jsonb.type(Tool.class);
    assertThat(type.fromJson("\"hammer\"")).isEqualTo(Tool.HAMMER);
    assertThat(type.fromJson("\"mallet\"")).isEqualTo(Tool.HAMMER);
    assertThat(type.fromJson("\"SCREWDRIVER\"")).isEqualTo(Tool.SCREWDRIVER);
    assertThat(type.fromJson("\"LEGACY\"")).isEqualTo(Tool.LEGACY);
    assertThat(type.fromJson("null")).isNull();
    assertThatThrownBy(() -> type.fromJson("\"HAMMER\"")).isInstanceOf(JsonException.class);
    assertThatThrownBy(() -> type.fromJson("\"SECRET\"")).isInstanceOf(JsonException.class);
  }

  @Test
  void mixInAsProperty() {
    final var box = new Toolbox(Tool.HAMMER, List.of(Tool.WRENCH, Tool.HAMMER));
    final var asJson = jsonb.toJson(box);
    assertThat(asJson).isEqualTo("{\"main\":\"hammer\",\"others\":[\"WRENCH\",\"hammer\"]}");
    assertThat(jsonb.type(Toolbox.class).fromJson("{\"main\":\"mallet\",\"others\":[\"hammer\"]}"))
      .isEqualTo(new Toolbox(Tool.HAMMER, List.of(Tool.HAMMER)));
  }

  @Test
  void jsonEnumWithAnnotatedConstants() {
    final var type = jsonb.type(Fruit.class);
    assertThat(type.toJson(Fruit.APPLE)).isEqualTo("\"apple\"");
    assertThat(type.toJson(Fruit.BANANA)).isEqualTo("\"BANANA\"");
    assertThat(type.fromJson("\"apple\"")).isEqualTo(Fruit.APPLE);
    assertThat(type.fromJson("\"nana\"")).isEqualTo(Fruit.BANANA);
    assertThatThrownBy(() -> type.toJson(Fruit.DURIAN)).isInstanceOf(JsonException.class);
    assertThatThrownBy(() -> type.fromJson("\"DURIAN\"")).isInstanceOf(JsonException.class);
  }
}
