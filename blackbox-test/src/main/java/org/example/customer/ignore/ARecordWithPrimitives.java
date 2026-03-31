package org.example.customer.ignore;

import io.avaje.jsonb.Json;

@Json
public record ARecordWithPrimitives(
  String one,
  @Json.Ignore boolean bool,
  @Json.Ignore int myInt,
  @Json.Ignore long myLong,
  @Json.Ignore double myDouble,
  @Json.Ignore float myFloat,
  @Json.Ignore short myShort,
  @Json.Ignore Object anything,
  String end
) {
}
