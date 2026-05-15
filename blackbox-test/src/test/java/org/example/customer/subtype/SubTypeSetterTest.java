package org.example.customer.subtype;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubTypeSetterTest {

  Jsonb jsonb = Jsonb.builder().build();
  JsonType<Machine> type = jsonb.type(Machine.class);

  @Test
  void v1_bloodTypeSetter_applied() {
    Machine result = type.fromJson("{\"@type\":\"Machine.V1\",\"bloodType\":\"hello\"}");
    assertThat(result).isInstanceOf(Machine.V1.class);
    assertThat(result.name).isEqualTo("hello");
  }

  @Test
  void v2_bloodTypeSetter_applied() {
    Machine result = type.fromJson("{\"@type\":\"Machine.V2\",\"bloodType\":\"world\"}");
    assertThat(result).isInstanceOf(Machine.V2.class);
    assertThat(result.name).isEqualTo("world");
  }

  @Test
  void v1_altFireSetter_applied() {
    Machine result = type.fromJson("{\"@type\":\"Machine.V1\",\"altFire\":\"foo\"}");
    assertThat(result).isInstanceOf(Machine.V1.class);
    assertThat(result.name).isEqualTo("foo");
  }

  @Test
  void v2_altFireSetter_applied() {
    Machine result = type.fromJson("{\"@type\":\"Machine.V2\",\"altFire\":\"bar\"}");
    assertThat(result).isInstanceOf(Machine.V2.class);
    assertThat(result.name).isEqualTo("bar");
  }
}
