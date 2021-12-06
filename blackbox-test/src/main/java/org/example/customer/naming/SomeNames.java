package org.example.customer.naming;

import io.avaje.jsonb.Json;

@Json
public record SomeNames(
  @Json.Property("$a")
  String a,

  String $b,

  @Json.Property("#")
  String hash,

  @Json.Property("$")
  String dollar,

  @Json.Property("$foo")
  String dollarFoo,

  @Json.Property("\"with quotes\"")
  String withQuotes
) {
}
