package org.example.customer.subtype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class SharedTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Shared> type = jsonb.type(Shared.class);

  @Test
  void toJson() {
    assertThat(type.toJson(new SharedText("a", "hi")))
      .isEqualTo("{\"@type\":\"text\",\"id\":\"a\",\"value\":\"hi\"}");
    assertThat(type.toJson(new SharedNum("b", 42)))
      .isEqualTo("{\"@type\":\"num\",\"id\":\"b\",\"value\":42}");
  }

  @Test
  void fromJson_sharedPropertyNameWithDifferentTypes() {
    Shared text = type.fromJson("{\"@type\":\"text\",\"id\":\"a\",\"value\":\"hi\"}");
    assertThat(text).isInstanceOf(SharedText.class);
    assertThat(text.id()).isEqualTo("a");
    assertThat(((SharedText) text).getValue()).isEqualTo("hi");

    Shared num = type.fromJson("{\"@type\":\"num\",\"id\":\"b\",\"value\":42}");
    assertThat(num).isInstanceOf(SharedNum.class);
    assertThat(((SharedNum) num).getValue()).isEqualTo(42);
  }

  @Test
  void fromJson_escapedTypeValue() {
    Shared num = type.fromJson("{\"id\":\"b\",\"@type\":\"n\\u0075m\"}");
    assertThat(num).isInstanceOf(SharedNum.class);
    assertThat(num.id()).isEqualTo("b");
  }

  @Test
  void fromJson_unknownType() {
    assertThatThrownBy(() -> type.fromJson("{\"@type\":\"other\",\"id\":\"b\"}"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Unknown value for @type property other");
  }

  @Test
  void fromJson_nullType() {
    assertThatThrownBy(() -> type.fromJson("{\"@type\":null,\"id\":\"b\"}"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Missing Required @type property");
  }

  @Test
  void roundTrip_otherSubtype() {
    TheRock rock = jsonb.type(TheRock.class).fromJson("{\"name\":\"n\",\"size\":1,\"@type\":\"Bocchi\",\"anxietyLvl\":7}");
    assertThat(rock).isInstanceOf(Bocchi.class);
    assertThat(((Bocchi) rock).getAnxietyLvl()).isEqualTo(7);
  }
}
