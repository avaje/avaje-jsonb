package org.example.customer.node;

import io.avaje.json.node.JsonObject;
import io.avaje.jsonb.Json;

@Json
public record HelloWithUnmapped(
  String name,
  int count,
  @Json.Unmapped
  JsonObject other) {
}
