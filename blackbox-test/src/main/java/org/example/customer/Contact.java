package org.example.customer;

import io.avaje.jsonb.Json;

import java.util.UUID;

@Json
public class Contact {

  private final UUID id;
  private final String firstName;
  private final String lastName;

  public Contact(UUID id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public UUID id() {
    return id;
  }

  public String firstName() {
    return firstName;
  }

  public String lastName() {
    return lastName;
  }
}
