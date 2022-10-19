package org.example.customer;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithAnnotationsTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void toJsonFromJson() {
    var bean = new WithAnnotations("foo","bar");
    String asJson = jsonb.toJson(bean);
    assertThat(asJson).isEqualTo("{\"one\":\"foo\",\"two\":\"bar\"}");

    WithAnnotations fromJson = jsonb.type(WithAnnotations.class).fromJson(asJson);

    assertThat(fromJson.one()).isEqualTo("foo");
    assertThat(fromJson.two()).isEqualTo("bar");
  }

}
