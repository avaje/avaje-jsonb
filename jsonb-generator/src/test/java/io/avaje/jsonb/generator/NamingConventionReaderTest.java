package io.avaje.jsonb.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NamingConventionReaderTest {

  @Test
  void naming_expected() {
    final Naming naming = NamingConventionReader.naming("LowerUnderscore");
    assertThat(naming).isEqualTo(Naming.LowerUnderscore);
  }

  @Test
  void naming_namingWithPrefix() {
    Naming naming = NamingConventionReader.naming("Naming.LowerUnderscore");
    assertThat(naming).isEqualTo(Naming.LowerUnderscore);

    naming = NamingConventionReader.naming("io.avaje.jsonb.Naming.LowerUnderscore");
    assertThat(naming).isEqualTo(Naming.LowerUnderscore);

    naming = NamingConventionReader.naming("io.avaje.jsonb.Naming.io.avaje.jsonb.Naming.LowerUnderscore");
    assertThat(naming).isEqualTo(Naming.LowerUnderscore);
  }

}
