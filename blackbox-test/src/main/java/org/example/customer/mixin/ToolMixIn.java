package org.example.customer.mixin;

import io.avaje.jsonb.Json;

@Json.MixIn(Tool.class)
public enum ToolMixIn {

  @Json.Property("hammer")
  @Json.Alias("mallet")
  HAMMER,

  @Json.Ignore
  SECRET,

  @Json.Ignore(deserialize = true)
  LEGACY
}
