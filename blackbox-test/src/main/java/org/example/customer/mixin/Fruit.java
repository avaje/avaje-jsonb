package org.example.customer.mixin;

import io.avaje.jsonb.Json;

@Json
public enum Fruit {
  @Json.Property("apple")
  APPLE,
  @Json.Alias("nana")
  BANANA,
  @Json.Ignore
  DURIAN
}
