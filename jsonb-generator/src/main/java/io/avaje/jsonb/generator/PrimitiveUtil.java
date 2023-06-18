package io.avaje.jsonb.generator;

import java.util.HashMap;
import java.util.Map;

final class PrimitiveUtil {

  static Map<String, String> wrapperMap = new HashMap<>();

  static {
    wrapperMap.put("char", "Character");
    wrapperMap.put("byte", "Byte");
    wrapperMap.put("int", "Integer");
    wrapperMap.put("long", "Long");
    wrapperMap.put("short", "Short");
    wrapperMap.put("double", "Double");
    wrapperMap.put("float", "Float");
    wrapperMap.put("boolean", "Boolean");
  }

  static String wrap(String shortName) {
    final String wrapped = wrapperMap.get(shortName);
    return wrapped != null ? wrapped : shortName;
  }

  static boolean isPrimitive(String typeShortName) {
    return wrapperMap.containsKey(typeShortName)
        || typeShortName.startsWith("Optional");
  }

  static String defaultValue(String shortType) {
    if (shortType.subString(0, 7).equals("Optional")) {
      if (shortType.contains("<")) {
        return "Optional.empty()";
      }

      return shortType + ".empty()";
    }

    return "boolean".equals(shortType) ? "false" : "0";
  }
}
