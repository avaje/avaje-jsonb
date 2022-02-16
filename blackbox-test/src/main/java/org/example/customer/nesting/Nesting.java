package org.example.customer.nesting;

import io.avaje.jsonb.Json;

import java.util.List;

public class Nesting {

  @Json
  public record One(String a, List<Two> twos, int b, Three three, long c) {
  }

  @Json
  public record Two(long f, Four four, String g) {

  }

  @Json
  public record Three(String other) {
  }

  @Json
  public record Four(String four) {
  }

}
