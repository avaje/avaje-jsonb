package io.avaje.jsonb.generator.models.valid;

import java.util.Optional;

import io.avaje.jsonb.Json;

@Json
public class OptionalAccess {

  String stringy;

  public Optional<String> stringy() {
    return Optional.of(stringy);
  }

  public void stringy(String stringy) {
    this.stringy = stringy;
  }
}
