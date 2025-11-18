package io.avaje.jsonb.generator.models.valid.nested;

import java.util.Map;

import io.avaje.jsonb.Json;

@Json
public class TestMapMap {
  Map<String, Map<String, Entity>> dims;

  @Json
  public static class Entity {
    String name;
    String value;
  }
}
