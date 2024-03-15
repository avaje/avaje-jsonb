package org.example.customer.creator;

import java.util.Map;

import io.avaje.jsonb.Json;

@Json
public record CreatorUnmapped(String a, String b) {

  @Json.Creator
  public static CreatorUnmapped fromJson(@Json.Unmapped Map<String, Object> json) {
    String aVal = (String)json.get("someA");
    String bVal = (String)json.get("someB");
    return new CreatorUnmapped(aVal, bVal);
  }
}
