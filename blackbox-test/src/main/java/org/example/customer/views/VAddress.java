package org.example.customer.views;

public class VAddress {

  String street;
  String suburb;
  String city;

  public String street() {
    return street;
  }

  public VAddress street(String street) {
    this.street = street;
    return this;
  }

  public String suburb() {
    return suburb;
  }

  public VAddress suburb(String suburb) {
    this.suburb = suburb;
    return this;
  }

  public String city() {
    return city;
  }

  public VAddress city(String city) {
    this.city = city;
    return this;
  }
}
