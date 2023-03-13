package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = Dwayne.class)
@Json.SubType(type = Bocchi.class)
public abstract class TheRock {

  protected final long size;

  protected final String name;

  protected TheRock(long size, String name) {
    this.size = size;
    this.name = name;
  }

  public long size() {
    return size;
  }

  public String name() {
    return name;
  }
}
