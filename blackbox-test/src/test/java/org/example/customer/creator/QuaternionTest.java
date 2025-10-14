package org.example.customer.creator;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuaternionTest {

  final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asJson() {
    var q = new Quaternion(0.1, 0.2, 0.3, 0.4);

    String json = jsonb.toJson(q);
    Quaternion fromJson = jsonb.type(Quaternion.class).fromJson(json);

    assertThat(fromJson.getW()).isEqualTo(0.1);
    assertThat(fromJson.getX()).isEqualTo(0.2);
    assertThat(fromJson.getY()).isEqualTo(0.3);
    assertThat(fromJson.getZ()).isEqualTo(0.4);
  }
}
