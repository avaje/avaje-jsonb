package io.avaje.json.stream.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParserLimitsTest {

  @Test
  void capsExplicitMaxStringBuffer() {
    var limits = new ParserLimits(309, Integer.MAX_VALUE, 10_000);

    assertThat(limits.maxStringBuffer()).isEqualTo(Integer.MAX_VALUE - 8);
  }
}
