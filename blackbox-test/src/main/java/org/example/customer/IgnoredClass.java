package org.example.customer;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Ignore;
import io.avaje.jsonb.Json.Property;

@Json
@Ignore(serialize = true)
public class IgnoredClass {

  private String firstName;

  @Json.Ignore private String middleName;

  @Property("last")
  private String lastName;

  public IgnoredClass() {}

  public IgnoredClass(String firstName, String middleName) {
    this.firstName = firstName;
    this.middleName = middleName;
  }

  public String getFirstName() {
    return firstName;
  }

  public IgnoredClass setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getMiddleName() {
    return middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public IgnoredClass setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
}
