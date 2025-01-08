package io.avaje.json.core;

import java.util.Arrays;
import java.util.List;

public class UtilList {

  public static <T> List<T> of(T... entries) {
    return Arrays.asList(entries);
  }

}
