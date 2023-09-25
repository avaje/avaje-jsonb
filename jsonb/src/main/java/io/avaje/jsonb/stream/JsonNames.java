package io.avaje.jsonb.stream;

import io.avaje.jsonb.spi.PropertyNames;

import java.util.*;

/**
 * Provides "prepared" JSON keys/field names for improved performance
 * during generation and parsing.
 */
final class JsonNames implements PropertyNames {

  static final JsonNames EMPTY = new JsonNames(new byte[0][0], Collections.emptyMap());
  private final byte[][] nameArray;
  private final Map<Integer, String> nameHash;
  private final int size;
  private final int[] hashes;
  private final String[] keys;

  JsonNames(byte[][] nameArray, Map<Integer, String> nameHash) {
    this.nameArray = nameArray;
    this.nameHash = nameHash;
    int inputSize = nameHash.size();
    if (inputSize > 50) {
      this.size = 0;
      this.hashes = new int[0];
      this.keys = new String[0];
    } else {
      // support linear match of hash values
      this.size = inputSize;
      this.hashes = new int[size];
      this.keys = new String[size];

      int pos = 0;
      for (Map.Entry<Integer, String> entry : nameHash.entrySet()) {
        hashes[pos] = entry.getKey();
        keys[pos] = entry.getValue();
        pos++;
      }
    }
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
    for (int i = 0; i < size; i++) {
      if (hashes[i] == hash) {
        return keys[i];
      }
    }
    return size != 0 ? null : nameHash.get(hash);
  }
}
