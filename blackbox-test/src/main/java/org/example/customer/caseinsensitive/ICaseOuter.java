package org.example.customer.caseinsensitive;

import java.util.Map;

import io.avaje.jsonb.Json;

@Json(caseInsensitiveKeys = true)
public record ICaseOuter(String name, HashInner inner, String after) {

  @Json
  public record HashInner(String name, Map<String, String> tags, ICaseContact contact, String after) {
  }
}
