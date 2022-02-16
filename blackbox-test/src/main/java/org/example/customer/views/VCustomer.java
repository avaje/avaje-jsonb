package org.example.customer.views;

import io.avaje.jsonb.Json;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Json
public class VCustomer {

  Integer id;
  String name;
  Instant whenCreated;
  VAddress billingAddress;
  List<VContact> contacts = new ArrayList<>();

  public Integer id() {
    return id;
  }

  public VCustomer id(Integer id) {
    this.id = id;
    return this;
  }

  public String name() {
    return name;
  }

  public VCustomer name(String name) {
    this.name = name;
    return this;
  }

  public Instant whenCreated() {
    return whenCreated;
  }

  public VCustomer whenCreated(Instant whenCreated) {
    this.whenCreated = whenCreated;
    return this;
  }

  public VAddress billingAddress() {
    return billingAddress;
  }

  public VCustomer billingAddress(VAddress billingAddress) {
    this.billingAddress = billingAddress;
    return this;
  }

  public List<VContact> contacts() {
    return contacts;
  }

  public VCustomer contacts(List<VContact> contacts) {
    this.contacts = contacts;
    return this;
  }
}
