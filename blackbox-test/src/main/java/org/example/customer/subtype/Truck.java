package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json
public class Truck extends Vehicle {

  private int capacity;

  public Truck(long id) {
    this.id = id;
  }

  public int capacity() {
    return capacity;
  }

  public void capacity(int capacity) {
    this.capacity = capacity;
  }
}
