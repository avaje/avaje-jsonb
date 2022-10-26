package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public class IgnoreField {

  private String firstName;

  @Json.Ignore
  private String middleName;

  private String lastName;

  public IgnoreField() {
  }

  public IgnoreField(String firstName, String middleName) {
    this.firstName = firstName;
    this.middleName = middleName;
  }

  public String getFirstName() {
    return firstName;
  }

  public IgnoreField setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public IgnoreField setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
}
