package org.example.customer.generics;

import io.avaje.jsonb.Json;

import java.util.List;

@Json
public class MyListWrapper<T> {

  private final List<T> list;

  public MyListWrapper(List<T> list) {
    this.list = list;
  }

  public List<T> list() {
    return list;
  }

}
