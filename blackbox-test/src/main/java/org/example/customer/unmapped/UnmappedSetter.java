package org.example.customer.unmapped;

import io.avaje.jsonb.Json;

import java.util.Map;

@Json
public class UnmappedSetter {

  private long id;
  private String name;
  private Map<String, Object> extra;

  public long id() {
    return id;
  }

  public UnmappedSetter id(long id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public UnmappedSetter name(String name) {
    this.name = name;
    return this;
  }

  public Map<String, Object> extra() {
    return extra;
  }

  @Json.Unmapped
  public UnmappedSetter extra(Map<String, Object> extra) {
    this.extra = extra;
    return this;
  }
}
