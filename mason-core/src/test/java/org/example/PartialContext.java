package org.example;

import io.avaje.jsonb.JsonAdapter;

import java.util.function.Supplier;

public class PartialContext {
  public boolean include(String id) {
    return false;
  }

  public <T> void add(String id, JsonAdapter<T> adapter, Supplier<T> supplier) {

  }
}
