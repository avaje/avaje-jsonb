package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.MixIn;

@MixIn(Kingfisher.class)
public interface KingfisherMixin {

  @Json.Creator
  static Kingfisher construct(String name) {
    return null;
  }
}
