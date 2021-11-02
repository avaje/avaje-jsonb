package io.avaje.jsonb.generator;

import java.util.HashMap;
import java.util.Map;

class GenericTypeMap {

  private final Map<String, String> basic = new HashMap<>();

  GenericTypeMap() {
    basic.put("java.lang.Integer", "Integer.TYPE");
    basic.put("java.lang.Long", "Long.TYPE");
    basic.put("java.lang.String", "String.class");
    basic.put("java.time.Instant", "Instant.class");
  }

  String typeOf(GenericType genericType) {
    String basicType = basic.get(genericType.raw());
    if (basicType != null) {
      return basicType;
    }
    return genericType.asTypeDeclaration();
  }

  String typeOfRaw(String rawType) {
    return basic.get(rawType);
  }
}
