package org.example.customer.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class PlainJsonEnumTest {

  final Jsonb jsonb = Jsonb.instance();

  @Test
  void rawSingleValue_toFromJson() {
    final JsonType<PlainJsonEnum> type = jsonb.type(PlainJsonEnum.class);

    assertThat(type.toJson(PlainJsonEnum.ONE)).isEqualTo("\"ONE\"");
    assertThat(type.toJson(PlainJsonEnum.TWO)).isEqualTo("\"TWO\"");

    assertThat(type.fromJson("\"ONE\"")).isEqualTo(PlainJsonEnum.ONE);
    assertThat(type.fromJson("\"TWO\"")).isEqualTo(PlainJsonEnum.TWO);
  }

  @Test
  void rawListOfValues_toFromJson() {
    final JsonType<List<PlainJsonEnum>> listType = jsonb.type(PlainJsonEnum.class).list();

    final List<PlainJsonEnum> values = List.of(PlainJsonEnum.ONE, PlainJsonEnum.TWO, PlainJsonEnum.ONE);
    final String json = listType.toJson(values);
    assertThat(json).isEqualTo("[\"ONE\",\"TWO\",\"ONE\"]");

    final List<PlainJsonEnum> fromJson = listType.fromJson(json);
    assertThat(fromJson).containsExactly(PlainJsonEnum.ONE, PlainJsonEnum.TWO, PlainJsonEnum.ONE);
  }

  @Test
  void wrappedInRecord_toFromJson() {
    final JsonType<PlainJsonEnumHolder> type = jsonb.type(PlainJsonEnumHolder.class);

    final var bean = new PlainJsonEnumHolder("bean-1", PlainJsonEnum.TWO, List.of(PlainJsonEnum.ONE, PlainJsonEnum.TWO));

    final String json = type.toJson(bean);
    assertThat(json).isEqualTo("{\"name\":\"bean-1\",\"value\":\"TWO\",\"values\":[\"ONE\",\"TWO\"]}");

    final PlainJsonEnumHolder fromJson = type.fromJson(json);
    assertThat(fromJson).isEqualTo(bean);
  }
}
