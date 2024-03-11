package io.avaje.jsonb.generator.models.valid;

import java.util.Map;

import io.avaje.jsonb.Json;

@Json
public class CreatorUnmapped {

  public String someObject;

  @Json.Creator
  public static CreatorUnmapped fromJson(@Json.Unmapped Map<String, Object> json) {
    return null;
  }
}
