package org.example.customer.alias;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithAliasTest {
  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    var bean = new WithAlias(42, "foo", "bar");
    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"id\":42,\"alias\":\"foo\",\"message\":\"bar\"}");

    JsonType<WithAlias> type = jsonb.type(WithAlias.class);
    WithAlias fromJson = type.fromJson(asJson);

    assertThat(fromJson).isEqualTo(bean);
    assertThat(type.fromJson("{\"id\":42,\"something\":\"foo\",\"message\":\"bar\"}")).isEqualTo(bean);
    assertThat(type.fromJson("{\"id\":42,\"something2\":\"foo\",\"message\":\"bar\"}")).isEqualTo(bean);
    assertThat(type.fromJson("{\"id\":42,\"alias\":\"foo\",\"message\":\"bar\"}")).isEqualTo(bean);
  }
}
