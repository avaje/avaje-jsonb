package org.example.customer.value;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class EnumWithAliasTest {

  @Json
  record Wrapper(EnumWithAlias value) {}

  final JsonType<Wrapper> type = Jsonb.instance().type(Wrapper.class);

  @Test
  void primaryName_roundTrip() {
    var bean = new Wrapper(EnumWithAlias.kNewName);
    var json = type.toJson(bean);
    assertThat(json).isEqualTo("{\"value\":\"kNewName\"}");
    assertThat(type.fromJson(json)).isEqualTo(bean);
  }

  @Test
  void alias_deserializes_to_primary() {
    assertThat(type.fromJson("{\"value\":\"kAlternateName\"}").value())
        .isEqualTo(EnumWithAlias.kNewName);
    assertThat(type.fromJson("{\"value\":\"kOldName\"}").value()).isEqualTo(EnumWithAlias.kNewName);
  }

  @Test
  void other_constant_unaffected() {
    var bean = new Wrapper(EnumWithAlias.kOther);
    var json = type.toJson(bean);
    assertThat(json).isEqualTo("{\"value\":\"kOther\"}");
    assertThat(type.fromJson(json)).isEqualTo(bean);
  }
}
