package org.example.customer.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

class StaffStatusTest {

  final JsonType<Staff> jsonb = Jsonb.builder().build().type(Staff.class);

  @Test
  void null_Enum_To_From_Json() {
    final var bean = new Staff();
    bean.setName("ZY");
    // bean.setStatus(...);
    final var str = jsonb.toJson(bean);
    assertThat(str).isEqualTo("{\"name\":\"ZY\"}");

    final var staff = jsonb.fromJson(str);
    assertThat(bean).isEqualTo(staff);
  }

  @Test
  void enum_To_From_Json() {
    final var bean = new Staff();
    bean.setName("ZY");
    bean.setStatus(StaffStatus.NORMAL);

    final var str = jsonb.toJson(bean);
    assertThat(str).isEqualTo("{\"name\":\"ZY\",\"status\":1}");
    final var staff = jsonb.fromJson(str);
    assertThat(bean).isEqualTo(staff);
  }
}
