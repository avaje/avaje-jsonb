package org.example.customer.views;

public class VContact {

  private final Long id;
  private final String firstName;
  private final String lastName;

  public VContact(Long id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public Long id() {
    return id;
  }

  public String firstName() {
    return firstName;
  }

  public String lastName() {
    return lastName;
  }
}
