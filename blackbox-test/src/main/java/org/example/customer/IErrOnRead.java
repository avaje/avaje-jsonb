package org.example.customer;

import io.avaje.jsonb.Json;

import java.util.UUID;

@Json
public record IErrOnRead(UUID id, String firstName, String lastName) {

  public String lastName() {
    throw new IllegalArgumentException("error reading lastName");
  }
}
