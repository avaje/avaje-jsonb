package org.example.customer.unmapped;

import io.avaje.jsonb.Json;

import java.util.Map;

@Json
public class UnmappedTwo {

  private final long id;
  private final String name;

  @Json.Ignore(deserialize = true)
  @Json.Unmapped
  private final Map<String,Object> unmapped;

  public UnmappedTwo(long id, String name, Map<String, Object> unmapped) {
    this.id = id;
    this.name = name;
    this.unmapped = unmapped;
  }

  public long id() {
    return id;
  }

  public String name() {
    return name;
  }

  public Map<String, Object> unmapped() {
    return unmapped;
  }

}
