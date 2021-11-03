package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public class Address {

  private final Long id;
  private final String street;
  private String suburb;
  private String city;

  public Address(Long id, String street) {
    this.id = id;
    this.street = street;
  }

  public Long getId() {
    return id;
  }

  public String getStreet() {
    return street;
  }

  public String getSuburb() {
    return suburb;
  }

  public void setSuburb(String suburb) {
    this.suburb = suburb;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
}
