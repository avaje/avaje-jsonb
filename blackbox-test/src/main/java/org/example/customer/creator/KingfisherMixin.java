package org.example.customer.creator;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.MixIn;

@MixIn(Kingfisher.class)
public interface KingfisherMixin {

  @Json.Creator
  static Kingfisher construct(String name) {
    var kf = new Kingfisher(name);
    kf.setFishCaught(42);
    return kf;
  }
}
