package io.avaje.jsonb.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.avaje.jsonb.Jsonb;

class TestBuilder {

  @Test
  void defaultBuilderReturnSameInstance() {

    assertThat(Jsonb.builder().build()).isSameAs(Jsonb.builder().build());
  }

  @Test
  void changedBuilderNotSame() {

    assertThat(Jsonb.builder().mathTypesAsString(true).build())
        .isNotSameAs(Jsonb.builder().build());
  }
}
