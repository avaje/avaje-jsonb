package io.avaje.json.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class UtilMap {

  public static <T> Map<String, T> of(String s0, T o0) {
    return of(s0, o0, null, null);
  }

  public static <T> Map<String, T> of(String s0, T o0, String s1, T o1){
    LinkedHashMap<String, T> map = new LinkedHashMap<>();
    map.put(s0, o0);
    if (s1 != null) {
      map.put(s1, o1);
    }
    return map;
  }
}
