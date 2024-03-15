package org.example.customer.creator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class CreatorUnmappedTest {

  final Jsonb jsonb = Jsonb.builder().build();

  @Test
  void asJson() {
    CreatorUnmapped fromJson = jsonb.type(CreatorUnmapped.class)
      .fromJson("{\"someA\":\"hi\",\"someB\":\"there\",\"fishCaught\":90}");

    assertThat(fromJson.a()).isEqualTo("hi");
    assertThat(fromJson.b()).isEqualTo("there");
  }
}
