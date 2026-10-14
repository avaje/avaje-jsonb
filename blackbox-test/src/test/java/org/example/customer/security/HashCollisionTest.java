package org.example.customer.security;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HashCollisionTest {

  final Jsonb jsonb = Jsonb.builder().build();
  final JsonType<AdminRecord> type = jsonb.type(AdminRecord.class);

  // "6ktbvb" has the same avaje name-hash (FNV-1a) as "admin" but is a different key.
  static final String COLLIDING = "{\"name\":\"alice\",\"6ktbvb\":true}";
  // Control: a genuine "admin" key.
  static final String GENUINE = "{\"name\":\"alice\",\"admin\":true}";

  @Test
  void collidingKey_fromString_mustNotSetAdmin() {
    AdminRecord bean = type.fromJson(COLLIDING);
    assertThat(bean.name()).isEqualTo("alice");
    assertThat(bean.admin())
      .as("key '6ktbvb' is not 'admin' and must not set the admin field")
      .isFalse();
  }

  @Test
  void collidingKey_fromInputStream_mustNotSetAdmin() {
    var in = new ByteArrayInputStream(COLLIDING.getBytes(StandardCharsets.UTF_8));
    AdminRecord bean = type.fromJson(in);
    assertThat(bean.name()).isEqualTo("alice");
    assertThat(bean.admin())
      .as("key '6ktbvb' is not 'admin' and must not set the admin field (stream path)")
      .isFalse();
  }

  @Test
  void genuineAdminKey_stillWorks_string() {
    AdminRecord bean = type.fromJson(GENUINE);
    assertThat(bean.admin()).isTrue();
  }

  @Test
  void genuineAdminKey_stillWorks_stream() {
    var in = new ByteArrayInputStream(GENUINE.getBytes(StandardCharsets.UTF_8));
    AdminRecord bean = type.fromJson(in);
    assertThat(bean.admin()).isTrue();
  }

  @Test
  void collidingKey_withUnmapped_goesToUnmappedNotAdmin() {
    JsonType<AdminUnmapped> t = jsonb.type(AdminUnmapped.class);
    AdminUnmapped bean = t.fromJson("{\"name\":\"alice\",\"6ktbvb\":true}");
    assertThat(bean.admin()).isFalse();
    assertThat(bean.unmapped()).containsKey("6ktbvb");
  }
}
