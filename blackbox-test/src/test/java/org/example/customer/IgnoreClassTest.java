package org.example.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class IgnoreClassTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    var bean = new IgnoredClass("fn", "mn");
    bean.setLastName("ln");

    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"firstName\":\"fn\",\"last\":\"ln\"}");

    var fromJson = jsonb.type(IgnoredClass.class).fromJson(asJson);
    assertThat(fromJson.getFirstName()).isNull();
    assertThat(fromJson.getLastName()).isNull();
    assertThat(fromJson.getMiddleName()).isNull();
  }
}
