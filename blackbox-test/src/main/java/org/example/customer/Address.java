package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public class Address {

  private Long id;
  private String street;
  private String suburb;
  private String city;
  private String funky;

  public Address(Long id, String street) {
    this.id = id;
    this.street = street;
  }

  /**
   * jsonb will use a default constructor if present.
   * <p>
   * This avoids the need for temporary variables in the fromJson() processing.
   */
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

  public String getcity() {
    return city;
  }

  /**
   * Intentionally does not match setter name by case - setcity vs setCity.
   */
  public void setcity(String city) {
    this.city = city;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String funky() {
    return funky;
  }

  public void setterDetermineByArgName(String funky) {
    this.funky = funky;
  }
}
