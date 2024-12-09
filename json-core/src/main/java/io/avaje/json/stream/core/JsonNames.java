package io.avaje.json.stream.core;

import io.avaje.json.PropertyNames;

import java.util.*;

/**
 * Provides "prepared" JSON keys/field names for improved performance
 * during generation and parsing.
 */
final class JsonNames implements PropertyNames {

  static final JsonNames EMPTY = new JsonNames(new byte[0][0], Collections.emptyMap());
  private final byte[][] nameArray;
  private final Map<Integer, String> nameHash;

  JsonNames(byte[][] nameArray, Map<Integer, String> nameHash) {
    this.nameArray = nameArray;
    this.nameHash = nameHash;
  }

  /**
   * Create given the names.
   */
  public static JsonNames of(String... names) {
    final Set<Integer> clashKeys = new HashSet<>();
    final Map<Integer, String> nameHash = new HashMap<>();
    final byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
      final int hash = Escape.nameHash(names[i]);
      final String priorKey = nameHash.put(hash, names[i]);
      if (priorKey != null && !priorKey.equals(names[i])) {
        clashKeys.add(hash);
      }
    }
    for (Integer clashKey : clashKeys) {
      nameHash.remove(clashKey);
    }
    return new JsonNames(nameArray, nameHash);
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  String lookup(int hash) {
     return nameHash.get(hash);
  }
}
