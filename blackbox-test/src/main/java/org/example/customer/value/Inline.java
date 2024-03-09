package org.example.customer.value;

import io.avaje.jsonb.Json;

@Json
public record Inline(String a, String b, Nested nested) {
  public record Nested(@Json.Value int a) {}
}
