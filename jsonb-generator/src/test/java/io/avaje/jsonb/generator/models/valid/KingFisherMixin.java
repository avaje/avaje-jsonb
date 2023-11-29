package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.MixIn;

@MixIn(KingFisher.class)
public interface KingFisherMixin {

  @Json.Creator
  static KingFisher construct(String name) {
    return null;
  }
}
