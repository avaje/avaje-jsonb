package org.example.customer.subtype;

public class Dog implements Animal {

  private long id;
  private final String name;

  public Dog(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  public long id() {
    return id;
  }

  public Dog id(long id) {
    this.id = id;
    return this;
  }
}
