package io.avaje.jsonb.generator;

import java.nio.charset.StandardCharsets;

final class Escape {

  static int nameHash(String name) {
    int hash = 0x811c9dc5;
    for (byte b : name.getBytes(StandardCharsets.UTF_8)) {
      if (b != '\\') {
        hash ^= b;
        hash *= 0x1000193;
      }
    }
    return hash;
  }
}
