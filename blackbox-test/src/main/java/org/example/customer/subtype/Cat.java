package org.example.customer.subtype;

public class Cat implements Animal {

  private String name;

  private AnimalEnum dtype;

  public AnimalEnum dtype() {
    return dtype;
  }

  public void dtype(AnimalEnum dtype) {
    this.dtype = dtype;
  }

  @Override
  public String name() {
    return name;
  }

  public Cat name(String name) {
    this.name = name;
    return this;
  }
}
