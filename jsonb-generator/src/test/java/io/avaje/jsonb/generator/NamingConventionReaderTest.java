package io.avaje.jsonb.generator;

import io.avaje.jsonb.Json;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NamingConventionReaderTest {

  @Test
  void naming_expected() {
    Json.Naming naming = NamingConventionReader.naming("LowerUnderscore");
    assertThat(naming).isEqualTo(Json.Naming.LowerUnderscore);
  }

  @Test
  void naming_namingWithPrefix() {
    Json.Naming naming = NamingConventionReader.naming("Naming.LowerUnderscore");
    assertThat(naming).isEqualTo(Json.Naming.LowerUnderscore);

    naming = NamingConventionReader.naming("io.avaje.jsonb.Json.Naming.LowerUnderscore");
    assertThat(naming).isEqualTo(Json.Naming.LowerUnderscore);

    naming = NamingConventionReader.naming("io.avaje.jsonb.Json.Naming.io.avaje.jsonb.Json.Naming.LowerUnderscore");
    assertThat(naming).isEqualTo(Json.Naming.LowerUnderscore);
  }

}
