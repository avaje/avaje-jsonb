package org.example.customer;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IgnoreFieldTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    var bean = new IgnoreField("fn", "mn");
    bean.setLastName("ln");

    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"firstName\":\"fn\",\"lastName\":\"ln\"}");

    IgnoreField fromJson = jsonb.type(IgnoreField.class).fromJson(asJson);
    assertThat(fromJson.getFirstName()).isEqualTo("fn");
    assertThat(fromJson.getLastName()).isEqualTo("ln");
    assertThat(fromJson.getMiddleName()).isNull();
  }
}
