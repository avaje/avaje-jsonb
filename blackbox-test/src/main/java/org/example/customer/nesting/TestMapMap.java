package org.example.customer.nesting;

import java.util.Map;

import io.avaje.jsonb.Json;

@Json
public record TestMapMap(Map<String, Map<String, Entity>> dims) {

  @Json
  public record Entity(String name, String value) {}
}
