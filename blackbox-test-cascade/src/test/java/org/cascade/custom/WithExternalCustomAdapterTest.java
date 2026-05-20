package org.cascade.custom;

import io.avaje.jsonb.Jsonb;
import org.example.other.custom.CustomClass;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WithExternalCustomAdapterTest {

  @Test
  void roundTrip() {
    final var jsonb = Jsonb.builder().build();
    final var value = new WithExternalCustomAdapter("hello", new CustomClass("world"));

    final var json = jsonb.toJson(value);
    assertThat(json).isEqualTo("{\"name\":\"hello\",\"custom\":{\"body\":\"world\"}}");

    final var deserialized = jsonb.type(WithExternalCustomAdapter.class).fromJson(json);
    assertThat(deserialized.name()).isEqualTo("hello");
    assertThat(deserialized.custom().body()).isEqualTo("world");
  }
}
