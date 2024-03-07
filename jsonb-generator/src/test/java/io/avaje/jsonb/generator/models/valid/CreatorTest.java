package io.avaje.jsonb.generator.models.valid;

import java.util.Map;

import io.avaje.jsonb.Json;

@Json
public class CreatorTest {

  public String someObject;

  @Json.Creator
  public static CreatorTest fromJson(@Json.Unmapped Map<String, Object> json) {
    return null;
  }
}
