package org.example.customer.subtype;

public class Ship extends Vehicle {

  String colour;

  public String colour() {
    return colour;
  }

  public void colour(String colour) {
    this.colour = colour;
  }
}
