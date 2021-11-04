package org.example.customer;

import io.avaje.jsonb.Json;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Json
public class Customer {

  public enum Status {
    NEW,
    ACTIVE,
    DISABLED
  }

  private Long id;
  private String name;
  private Status status;
  private Instant whenCreated;
  private List<Contact> contacts = new ArrayList<>();

  public Long id() {
    return id;
  }

  public Customer id(Long id) {
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

  public Status status() {
    return status;
  }

  public Customer status(Status status) {
    this.status = status;
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
