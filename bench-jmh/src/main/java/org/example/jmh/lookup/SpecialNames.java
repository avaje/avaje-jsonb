package org.example.jmh.lookup;

import io.avaje.jsonb.Json;

@Json
public record SpecialNames(
  @Json.Property("café") String cafe,
  @Json.Property("naïve") String naive,
  String plain
) {
}
