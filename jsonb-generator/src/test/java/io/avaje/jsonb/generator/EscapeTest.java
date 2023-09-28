package io.avaje.jsonb.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EscapeTest {

  @Test
  void nameHash() {
    int hash = Escape.nameHash("\"with quotes\"");
    assertThat(hash).isEqualTo(523918728);
  }
}
