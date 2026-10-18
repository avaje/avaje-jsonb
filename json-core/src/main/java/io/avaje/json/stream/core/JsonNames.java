package io.avaje.json.stream.core;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;

/**
 * Provides "prepared" JSON keys/field names for improved performance
 * during generation and parsing.
 */
final class JsonNames implements PropertyNames {

  static final JsonNames EMPTY = new JsonNames(new byte[0][0], new int[0], new String[0]);
  private final byte[][] nameArray;
  private final int[] hashes;
  private final String[] names;

  JsonNames(byte[][] nameArray, int[] hashes, String[] names) {
    this.nameArray = nameArray;
    this.hashes = hashes;
    this.names = names;
  }

  /**
   * Create given the names.
   */
  public static JsonNames of(String... names) {
    final Set<Integer> clashKeys = new HashSet<>();
    final Map<Integer, String> nameHash = new LinkedHashMap<>();
    final byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
      final int hash = JsonReader.nameHash(names[i]);
      final String priorKey = nameHash.put(hash, names[i]);
      if (priorKey != null && !priorKey.equals(names[i])) {
        clashKeys.add(hash);
      }
    }
    for (Integer clashKey : clashKeys) {
      nameHash.remove(clashKey);
    }
    final int[] hashes = new int[nameHash.size()];
    final String[] lookupNames = new String[nameHash.size()];
    int pos = 0;
    for (Map.Entry<Integer, String> entry : nameHash.entrySet()) {
      hashes[pos] = entry.getKey();
      lookupNames[pos++] = entry.getValue();
    }
    return new JsonNames(nameArray, hashes, lookupNames);
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  String lookup(int hash) {
    final int[] hashes = this.hashes;
    for (int i = 0; i < hashes.length; i++) {
      if (hashes[i] == hash) {
        return names[i];
      }
    }
    return null;
  }
}
