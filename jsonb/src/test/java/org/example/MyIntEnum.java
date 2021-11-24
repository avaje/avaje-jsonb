package org.example;

import io.avaje.jsonb.Json;

public enum MyIntEnum {

  ONE(97),
  TWO(98);

  final int val;
  MyIntEnum(int val) {
    this.val = val;
  }

  @Json.Value
  public int customJsonIntValue() {
    return val;
  }
}
