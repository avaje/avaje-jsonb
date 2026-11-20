package org.example.customer.hash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.example.customer.subtype.Shared;
import org.example.customer.subtype.SharedNum;
import org.junit.jupiter.api.Test;

import io.avaje.json.JsonReader;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class CollideTest {

  Jsonb jsonb = Jsonb.builder().strictPropertyNames(true).build();

  @Test
  void collidingKeys() {
    assertThat(JsonReader.nameHash("6ktbvb")).isEqualTo(JsonReader.nameHash("admin"));
    assertThat(JsonReader.nameHash("YzhoyT")).isEqualTo(JsonReader.nameHash("name"));
    assertThat(JsonReader.nameHash("nnac10")).isEqualTo(JsonReader.nameHash("isAdmin"));
    assertThat(JsonReader.nameHash("MVIbtr")).isEqualTo(JsonReader.nameHash("@type"));
    assertThat(JsonReader.nameHash("5wGry1")).isEqualTo(JsonReader.nameHash("value"));
  }

  @Test
  void notStrict_matchesByHashOnly() {
    final var type = Jsonb.builder().build().type(Collide.AdminRecord.class);
    assertThat(type.fromJson("{\"6ktbvb\":true}").admin()).isTrue();
  }

  @Test
  void record() {
    final var type = jsonb.type(Collide.AdminRecord.class);
    final var json = "{\"name\":\"alice\",\"6ktbvb\":true,\"YzhoyT\":\"bob\"}";
    assertThat(type.fromJson(json)).isEqualTo(new Collide.AdminRecord("alice", false));
    assertThat(type.fromJson(stream(json))).isEqualTo(new Collide.AdminRecord("alice", false));
    assertThat(type.fromJson("{\"name\":\"alice\",\"admin\":true}"))
        .isEqualTo(new Collide.AdminRecord("alice", true));
  }

  @Test
  void record_fromObject() {
    final var type = jsonb.type(Collide.AdminRecord.class);
    assertThat(type.fromObject(Map.of("name", "alice", "6ktbvb", true)))
        .isEqualTo(new Collide.AdminRecord("alice", false));
    assertThat(type.fromObject(Map.of("name", "alice", "admin", true)))
        .isEqualTo(new Collide.AdminRecord("alice", true));
  }

  @Test
  void record_escapedKey() {
    final var type = jsonb.type(Collide.AdminRecord.class);
    assertThat(type.fromJson("{\"6ktbv\\u0062\":true}"))
        .isEqualTo(new Collide.AdminRecord(null, false));
    assertThat(type.fromJson("{\"admi\\u006e\":true}"))
        .isEqualTo(new Collide.AdminRecord(null, true));
  }

  @Test
  void record_streamAcrossBufferBoundary() {
    final var type = jsonb.type(Collide.AdminRecord.class);
    // move the colliding key across the end of the stream buffer
    for (var pad = 0; pad < 300; pad++) {
      final var json = "{\"name\":\"" + "x".repeat(pad) + "\",\"6ktbvb\":true}";
      final var genuine = "{\"name\":\"" + "x".repeat(pad) + "\",\"admin\":true}";
      assertThat(type.fromJson(stream(json)).admin()).isFalse();
      assertThat(type.fromJson(stream(genuine)).admin()).isTrue();
    }
  }

  @Test
  void alias() {
    final JsonType<Collide.AdminAlias> type = jsonb.type(Collide.AdminAlias.class);
    assertThat(type.fromJson("{\"6ktbvb\":true}").admin()).isFalse();
    assertThat(type.fromJson("{\"nnac10\":true}").admin()).isFalse();
    assertThat(type.fromJson("{\"admin\":true}").admin()).isTrue();
    assertThat(type.fromJson("{\"isAdmin\":true}").admin()).isTrue();
  }

  @Test
  void unmapped() {
    final JsonType<Collide.AdminUnmapped> type = jsonb.type(Collide.AdminUnmapped.class);
    final var json = "{\"name\":\"alice\",\"6ktbvb\":true}";
    for (final Collide.AdminUnmapped bean :
        new Collide.AdminUnmapped[] {type.fromJson(json), type.fromJson(stream(json))}) {
      assertThat(bean.name).isEqualTo("alice");
      assertThat(bean.admin).isFalse();
      assertThat(bean.unmapped).isEqualTo(Map.of("6ktbvb", true));
    }
  }

  @Test
  void bean() {
    final JsonType<Collide.AdminBean> type = jsonb.type(Collide.AdminBean.class);
    final var bean = type.fromJson("{\"YzhoyT\":\"bob\",\"6ktbvb\":true}");
    assertThat(bean.getName()).isNull();
    assertThat(bean.isAdmin()).isFalse();
  }

  @Test
  void failOnUnknown() {
    final var type =
        Jsonb.builder()
            .strictPropertyNames(true)
            .failOnUnknown(true)
            .build()
            .type(Collide.AdminRecord.class);
    assertThatThrownBy(() -> type.fromJson("{\"6ktbvb\":true}")).hasMessageContaining("6ktbvb");
  }

  @Test
  void subtype_typeProperty() {
    final var type = jsonb.type(Shared.class);
    assertThatThrownBy(() -> type.fromJson("{\"MVIbtr\":\"num\",\"id\":\"b\"}"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void subtype_commonField() {
    final var type = jsonb.type(Shared.class);
    final var num = type.fromJson("{\"@type\":\"num\",\"id\":\"b\",\"5wGry1\":42}");
    assertThat(num).isInstanceOf(SharedNum.class);
    assertThat(((SharedNum) num).getValue()).isEqualTo(0);
  }

  private static ByteArrayInputStream stream(String json) {
    return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
  }
}
