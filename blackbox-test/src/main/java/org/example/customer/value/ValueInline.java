package org.example.customer.value;

import org.example.customer.Address;

import io.avaje.jsonb.Json;

@Json
public record ValueInline(int a, Nested nested, Nested2 nested2, Nested3 nested3, Nested4 complex) {
  public record Nested(@Json.Value int nestA, String nestB) {
    public Nested(int nestA) {
      this(nestA, "idk");
    }
  }

  public record Nested2(@Json.Value int nestb, String nestB) {

    @Json.Creator
    public static Nested2 of(int nestB) {
      return new Nested2(nestB, "somethin");
    }
  }

  public record Nested3(@Json.Value Nested2 nesting) {}

  public static class Nested4 {
    private final Address address;

    public Nested4(Address address) {
      this.address = address;
    }

    @Json.Value
    public Address address() {
      return address;
    }
  }
}
