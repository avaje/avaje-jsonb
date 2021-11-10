package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(Car.class)
@Json.SubType(value = Truck.class, name = "TRUCK")
public abstract class Vehicle {

  protected long id;

  protected String name;

  public long id() {
    return id;
  }

  public void id(long id) {
    this.id = id;
  }

  public String name() {
    return name;
  }

  public void name(String name) {
    this.name = name;
  }
}
