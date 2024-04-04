package org.example.customer.optional;

import java.util.Optional;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Property;

@Json
public record OptionalAccess(String stringy) {

  @Property("stringy")
  public Optional<String> stringyOp() {
    return Optional.ofNullable(stringy);
  }
}
