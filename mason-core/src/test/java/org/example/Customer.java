package org.example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Customer {

  Integer id;
  String name;
  Instant whenCreated;
  List<Contact> contacts = new ArrayList<>();

  public Integer id() {
    return id;
  }

  public Customer id(Integer id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public Customer name(String name) {
    this.name = name;
    return this;
  }

  public Instant whenCreated() {
    return whenCreated;
  }

  public Customer whenCreated(Instant whenCreated) {
    this.whenCreated = whenCreated;
    return this;
  }

  public List<Contact> contacts() {
    return contacts;
  }

  public Customer contacts(List<Contact> contacts) {
    this.contacts = contacts;
    return this;
  }
}
