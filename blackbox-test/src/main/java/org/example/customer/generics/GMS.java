package org.example.customer.generics;

import io.avaje.jsonb.Json;

public class GMS {

  public static class Weaver {

    @Json
    public static class Hornet<T> {
      public T value;
    }
  }
}
