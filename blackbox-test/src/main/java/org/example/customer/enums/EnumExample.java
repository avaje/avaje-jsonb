package org.example.customer.enums;

import java.util.EnumMap;
import java.util.Map;

import io.avaje.jsonb.Json;

@Json
public record EnumExample(String name, EnumMap<Thing, String> thingMap) {

  public enum Thing {
    ONE,
    TWO
  }
}
