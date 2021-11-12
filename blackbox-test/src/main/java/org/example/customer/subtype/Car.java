package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json
public class Car extends Vehicle {

  String colour;

  public String colour() {
    return colour;
  }

  public void colour(String colour) {
    this.colour = colour;
  }
}
