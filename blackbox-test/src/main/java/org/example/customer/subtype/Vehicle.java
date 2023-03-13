package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = Car.class)
@Json.SubType(type = Ship.class)
@Json.SubType(type = Truck.class, name = "TRUCK")
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
