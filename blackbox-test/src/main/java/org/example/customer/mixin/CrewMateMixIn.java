package org.example.customer.mixin;

import io.avaje.jsonb.Json;

@Json.MixIn(CrewMate.class)
public abstract class CrewMateMixIn {

  @Json.Property("color")
  private String c;

  @Json.Ignore(deserialize = true)
  private Integer susLv;

  @Json.Property("wrongtype")
  private int taskNumber;
}
