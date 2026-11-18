package org.example.customer.subtype;

import io.avaje.jsonb.Json;

/** Subtypes sharing a property name with different types. */
@Json
@Json.SubType(type = SharedText.class, name = "text")
@Json.SubType(type = SharedNum.class, name = "num")
public abstract class Shared {

  protected final String id;

  protected Shared(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
