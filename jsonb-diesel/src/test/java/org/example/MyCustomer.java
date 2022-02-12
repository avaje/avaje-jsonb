package org.example;

import io.avaje.jsonb.Json;

@Json
public class MyCustomer {

  final int id;
  final String name;
  final String notes;

  public MyCustomer(int id, String name, String notes) {
    this.id = id;
    this.name = name;
    this.notes = notes;
  }

  public int id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String notes() {
    return notes;
  }
}
