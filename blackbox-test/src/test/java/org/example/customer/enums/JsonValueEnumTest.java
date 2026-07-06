package org.example.customer.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class JsonValueEnumTest {

  final Jsonb jsonb = Jsonb.instance();

  @Test
  void rawSingleValue_toFromJson() {
    final JsonType<JsonValueEnum> type = jsonb.type(JsonValueEnum.class);

    assertThat(type.toJson(JsonValueEnum.ONE)).isEqualTo("\"one-code\"");
    assertThat(type.toJson(JsonValueEnum.TWO)).isEqualTo("\"two-code\"");

    assertThat(type.fromJson("\"one-code\"")).isEqualTo(JsonValueEnum.ONE);
    assertThat(type.fromJson("\"two-code\"")).isEqualTo(JsonValueEnum.TWO);
  }

  @Test
  void rawListOfValues_toFromJson() {
    final JsonType<List<JsonValueEnum>> listType = jsonb.type(JsonValueEnum.class).list();

    final List<JsonValueEnum> values = List.of(JsonValueEnum.ONE, JsonValueEnum.TWO, JsonValueEnum.ONE);
    final String json = listType.toJson(values);
    assertThat(json).isEqualTo("[\"one-code\",\"two-code\",\"one-code\"]");

    final List<JsonValueEnum> fromJson = listType.fromJson(json);
    assertThat(fromJson).containsExactly(JsonValueEnum.ONE, JsonValueEnum.TWO, JsonValueEnum.ONE);
  }

  @Test
  void wrappedInRecord_toFromJson() {
    final JsonType<JsonValueEnumHolder> type = jsonb.type(JsonValueEnumHolder.class);

    final var bean = new JsonValueEnumHolder("bean-1", JsonValueEnum.TWO, List.of(JsonValueEnum.ONE, JsonValueEnum.TWO));

    final String json = type.toJson(bean);
    assertThat(json).isEqualTo("{\"name\":\"bean-1\",\"value\":\"two-code\",\"values\":[\"one-code\",\"two-code\"]}");

    final JsonValueEnumHolder fromJson = type.fromJson(json);
    assertThat(fromJson).isEqualTo(bean);
  }
}
