package org.example.customer.creator;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KingFisherTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asJson() {
    Kingfisher kf = new Kingfisher("hi");
    kf.setFishCaught(90);

    String asJson = jsonb.toJson(kf);
    assertThat(asJson).isEqualTo("{\"name\":\"hi\",\"fishCaught\":90}");

    Kingfisher fromJson = jsonb.type(Kingfisher.class).fromJson(asJson);
    assertThat(fromJson.getName()).isEqualTo("hi");
    assertThat(fromJson.getFishCaught()).isEqualTo(42);
  }
}
