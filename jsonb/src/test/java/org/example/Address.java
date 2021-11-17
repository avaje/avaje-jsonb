package org.example;

public class Address {

  String street;
  String suburb;
  String city;

  public String street() {
    return street;
  }

  public Address street(String street) {
    this.street = street;
    return this;
  }

  public String suburb() {
    return suburb;
  }

  public Address suburb(String suburb) {
    this.suburb = suburb;
    return this;
  }

  public String city() {
    return city;
  }

  public Address city(String city) {
    this.city = city;
    return this;
  }
}
