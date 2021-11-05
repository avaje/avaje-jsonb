package org.example.customer.naming;

import io.avaje.jsonb.Json;

import static io.avaje.jsonb.Json.Naming.LowerHyphen;

@Json(naming = LowerHyphen)
public record WithName(
  @Json.Property("Some Thing Odd")
  String simple,
  String simplePlus,
  int myOneRed) {

}
