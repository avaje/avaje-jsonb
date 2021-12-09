package org.example;

import io.avaje.jsonb.Json;

public enum MyEnum {

  ONE("one val"),
  TWO("two val");

  final String val;
  MyEnum(String val) {
    this.val = val;
  }

  @Json.Value
  public String value() {
    return val;
  }
}
