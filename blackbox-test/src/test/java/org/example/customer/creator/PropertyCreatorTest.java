package org.example.customer.creator;

import io.avaje.jsonb.Jsonb;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyCreatorTest {

  final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asJson() {
    var instant = Instant.ofEpochMilli(1);
    var pc = new PropertyCreator("strVal", instant);

    String json = jsonb.toJson(pc);
    PropertyCreator fromJson = jsonb.type(PropertyCreator.class).fromJson(json);

    assertThat(fromJson.identifier()).isEqualTo("strVal");
    assertThat(fromJson.identifier()).isEqualTo(pc.identifier());
    assertThat(fromJson.startup()).isEqualTo(pc.startup());
    assertThat(fromJson.uptime()).isEqualTo(pc.uptime());
  }
}
