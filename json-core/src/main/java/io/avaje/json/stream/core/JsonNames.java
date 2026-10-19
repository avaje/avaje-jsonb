package io.avaje.json.stream.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;

/**
 * Provides "prepared" JSON keys/field names for improved performance
 * during generation and parsing.
 */
final class JsonNames implements PropertyNames {

  static final JsonNames EMPTY = of();
  private final String[] allNames;
  private final byte[][] nameArray;
  /** Open addressing table of name hash to name position, -1 for an empty slot. */
  private final int[] hashes;
  private final int[] positions;
  private final int mask;

  JsonNames(String[] allNames, byte[][] nameArray, int[] hashes, int[] positions) {
    this.allNames = allNames;
    this.nameArray = nameArray;
    this.hashes = hashes;
    this.positions = positions;
    this.mask = positions.length - 1;
  }

  /**
   * Create given the names.
   */
  public static JsonNames of(String... names) {
    final var clashKeys = new HashSet<Integer>();
    final var namePosition = new LinkedHashMap<Integer, Integer>();
    final byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
      final int hash = JsonReader.nameHash(names[i]);
      final Integer priorPos = namePosition.putIfAbsent(hash, i);
      if (priorPos != null && !names[priorPos].equals(names[i])) {
        clashKeys.add(hash);
      }
    }
    for (var clashKey : clashKeys) {
      namePosition.remove(clashKey);
    }
    final int size = Integer.highestOneBit(Math.max(1, namePosition.size()) * 2 - 1) << 1;
    final int[] hashes = new int[size];
    final int[] positions = new int[size];
    Arrays.fill(positions, -1);
    for (Map.Entry<Integer, Integer> entry : namePosition.entrySet()) {
      final int hash = entry.getKey();
      int slot = slot(hash) & (size - 1);
      while (positions[slot] != -1) {
        slot = (slot + 1) & (size - 1);
      }
      hashes[slot] = hash;
      positions[slot] = entry.getValue();
    }
    return new JsonNames(names.clone(), nameArray, hashes, positions);
  }

  @Override
  public String name(int position) {
    return allNames[position];
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  String lookup(int hash) {
    final int pos = position(hash);
    return pos < 0 ? null : allNames[pos];
  }

  /**
   * Return the position of the name with the given hash or -1 when there is no such name (or the
   * hash is shared by more than one name).
   */
  int position(int hash) {
    int slot = slot(hash) & mask;
    int pos;
    while ((pos = positions[slot]) != -1) {
      if (hashes[slot] == hash) {
        return pos;
      }
      slot = (slot + 1) & mask;
    }
    return -1;
  }

  private static int slot(int hash) {
    return hash ^ (hash >>> 16);
  }
}
