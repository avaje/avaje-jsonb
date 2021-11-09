package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public class Address {

  private Long id;
  private String street;
  private String suburb;
  private String city;

  public Address(Long id, String street) {
    this.id = id;
    this.street = street;
  }

  public Address() {
    // intentionally blank
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

  public void setId(Long id) {
    this.id = id;
  }

  public void setStreet(String street) {
    this.street = street;
  }
}
