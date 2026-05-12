package org.example.customer.setter;

import io.avaje.jsonb.Json;

@Json
public class PropertySetter {

  private long id;
  private String name;

  public long id() {
    return id;
  }

  public PropertySetter id(long id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  @Json.Property("full_name")
  public PropertySetter name(String name) {
    this.name = name;
    return this;
  }

  @Json.Property("void")
  public void voided(String name) {}
}
