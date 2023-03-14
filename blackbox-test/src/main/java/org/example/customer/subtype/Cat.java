package org.example.customer.subtype;

public class Cat implements Animal {


  private String dtype;

  private String name;


  public String dtype() {
    return dtype;
  }

  public void dtype(String dtype) {
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
