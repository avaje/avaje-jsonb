package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json
public enum EnumAliasTest {
  @Json.Alias({"Jane", "Juliet"})
  RYU,
  YUTA;

  @Json.Value
  @Override
  public String toString() {
    return super.toString();
  }
}
