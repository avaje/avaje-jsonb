package org.example.customer;

import io.avaje.jsonb.Json;

import java.time.Instant;

@Json
public class Customer {

  private Long id;
  private String name;
  private Instant whenCreated;

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

  public Instant whenCreated() {
    return whenCreated;
  }

  public Customer whenCreated(Instant whenCreated) {
    this.whenCreated = whenCreated;
    return this;
  }
}
