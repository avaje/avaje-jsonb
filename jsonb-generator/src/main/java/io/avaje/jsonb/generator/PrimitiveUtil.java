package io.avaje.jsonb.generator;

import java.util.HashMap;
import java.util.Map;

class PrimitiveUtil {

  static Map<String,String> wrapperMap = new HashMap<>();
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
    String wrapped = wrapperMap.get(shortName);
    return wrapped != null ? wrapped : shortName;
  }

  static boolean isPrimitive(String typeShortName) {
    return wrapperMap.containsKey(typeShortName);
  }

  static String defaultValue(String shortType) {
    return "boolean".equals(shortType) ? "false" : "0";
  }
}
