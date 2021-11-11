package org.example.customer.subtype;

public class Cat implements Animal {

  private String name;

  @Override
  public String name() {
    return name;
  }

  public Cat name(String name) {
    this.name = name;
    return this;
  }
}
