package io.avaje.jsonb.generator;

import java.util.HashMap;
import java.util.Map;

final class PrimitiveUtil {

  private static Map<String, String> wrapperMap = new HashMap<>();

  static {
    wrapperMap.put("char", "Character");
    wrapperMap.put("byte", "Byte");
    wrapperMap.put("int", "Integer");
    wrapperMap.put("long", "Long");
    wrapperMap.put("short", "Short");
    wrapperMap.put("double", "Double");
    wrapperMap.put("float", "Float");
    wrapperMap.put("boolean", "Boolean");
    // optionals
    wrapperMap.put("OptionalInt", "OptionalInt");
    wrapperMap.put("OptionalDouble", "OptionalDouble");
    wrapperMap.put("OptionalLong", "OptionalLong");
  }

  static String wrap(String shortName) {
    final String wrapped = wrapperMap.get(shortName);
    return wrapped != null ? wrapped : shortName;
  }

  static boolean isPrimitive(String typeShortName) {
    return wrapperMap.containsKey(typeShortName) || typeShortName.startsWith("Optional<");
  }

  static String defaultValue(String shortType) {
    if (shortType.startsWith("Optional")) {
      if (shortType.contains("<")) {
        return "Optional.empty()";
      }
      return shortType + ".empty()";
    }

    return "boolean".equals(shortType) ? "false" : "0";
  }
}
