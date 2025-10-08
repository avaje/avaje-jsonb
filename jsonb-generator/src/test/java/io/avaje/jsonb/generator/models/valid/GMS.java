package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

public class GMS {

  public static class Weaver {

    @Json
    public static class Hornet<T> {
      public T value;
    }
  }
}
