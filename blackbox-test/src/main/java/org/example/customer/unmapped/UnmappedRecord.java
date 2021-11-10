package org.example.customer.unmapped;

import io.avaje.jsonb.Json;

import java.util.Map;

@Json
public record UnmappedRecord (

  long id,
  String name,
  @Json.Unmapped
  Map<String,Object> unmapped
) {

}
