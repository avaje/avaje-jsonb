package org.example.customer.enums;

import java.util.Map;


import io.avaje.jsonb.Json;

@Json
public record EnumExample(String name, Map<Thing, String> thingMap) {

  public enum Thing {
    ONE,
    TWO
  }
}
