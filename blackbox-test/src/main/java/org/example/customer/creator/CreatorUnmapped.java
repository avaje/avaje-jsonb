package org.example.customer.creator;

import java.util.Map;

import io.avaje.jsonb.Json;

@Json
public record CreatorUnmapped(String someObject) {

  @Json.Creator
  public static CreatorUnmapped fromJson(@Json.Unmapped Map<String, Object> json) {
    return new CreatorUnmapped((String) json.get("unmapped"));
  }
}
