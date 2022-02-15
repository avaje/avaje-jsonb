package org.example.customer;

import io.avaje.jsonb.Json;

public class Nested {

  @Json
  public static record MyNest (int id, String name) {}
}
