package org.example.customer.creator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class CreatorUnmappedTest {

  Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asJson() {

    CreatorUnmapped fromJson =
        jsonb.type(CreatorUnmapped.class).fromJson("{\"unmapped\":\"hi\",\"fishCaught\":90}");
    assertThat(fromJson.someObject()).isEqualTo("hi");
  }
}
