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
  @Json.Ignore
  private String mySecret1;
  @Json.Ignore(serialize = true)
  private String mySecret2;
  @Json.Ignore(deserialize = true)
  private String mySecret3;

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

  public String getMySecret1() {
    return mySecret1;
  }

  public Customer mySecret1(String mySecret1) {
    this.mySecret1 = mySecret1;
    return this;
  }

  public String getMySecret2() {
    return mySecret2;
  }

  public Customer mySecret2(String mySecret2) {
    this.mySecret2 = mySecret2;
    return this;
  }

  public String getMySecret3() {
    return mySecret3;
  }

  public Customer mySecret3(String mySecret3) {
    this.mySecret3 = mySecret3;
    return this;
  }
}
