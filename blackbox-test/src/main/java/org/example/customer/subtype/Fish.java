package org.example.customer.subtype;

public class Fish implements Animal {

  private String name;

  @Override
  public String name() {
    return name;
  }

  public Fish name(String name) {
    this.name = name;
    return this;
  }
}
