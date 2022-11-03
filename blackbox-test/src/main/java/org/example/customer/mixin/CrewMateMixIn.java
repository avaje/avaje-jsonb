package org.example.customer.mixin;

import io.avaje.jsonb.Json;

@Json.Import(CrewMate.class)
@Json.MixIn(CrewMate.class)
public abstract class CrewMateMixIn {

  @Json.Property("color")
  private String c;

  @Json.Ignore(deserialize = true)
  private int susLv;
}
