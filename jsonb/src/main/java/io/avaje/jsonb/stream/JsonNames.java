package io.avaje.jsonb.stream;

import io.avaje.jsonb.spi.PropertyNames;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides "prepared" JSON keys/field names for improved performance
 * during generation and parsing.
 */
final class JsonNames implements PropertyNames {

  private final byte[][] nameArray;
  private final Map<Long, String> nameHash;

  JsonNames(byte[][] nameArray, Map<Long, String> nameHash) {
    this.nameArray = nameArray;
    this.nameHash = nameHash;
  }

  /**
   * Create given the names.
   */
  public static JsonNames of(String... names) {
    boolean hashingClash = false;
    Map<Long, String> nameHash = new HashMap<>();
    byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
      long hash = Escape.nameHash(names[i]);
      if (nameHash.put(hash, names[i]) != null) {
        hashingClash = true;
      }
    }
    return new JsonNames(nameArray, hashingClash ? Collections.emptyMap() : nameHash);
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  String lookup(long hash) {
    return nameHash.get(hash);
  }
}
