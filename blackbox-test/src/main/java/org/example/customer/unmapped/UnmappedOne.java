package org.example.customer.unmapped;

import io.avaje.jsonb.Json;

import java.util.Map;

@Json
public class UnmappedOne {

  private long id;
  private String name;

  @Json.Unmapped
  private Map<String,Object> unmapped;

  public long id() {
    return id;
  }

  public UnmappedOne id(long id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public UnmappedOne name(String name) {
    this.name = name;
    return this;
  }

  public Map<String, Object> unmapped() {
    return unmapped;
  }

  public UnmappedOne unmapped(Map<String, Object> unmapped) {
    this.unmapped = unmapped;
    return this;
  }
}
