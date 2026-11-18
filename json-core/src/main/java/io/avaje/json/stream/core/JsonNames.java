package io.avaje.json.stream.core;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

  static final JsonNames EMPTY = of();
  private final String[] allNames;
  private final byte[][] nameArray;
  /** The unescaped UTF-8 bytes of each name. */
  private final byte[][] rawArray;
  /** Open addressing table of name hash to name position, -1 for an empty slot. */
  private final int[] hashes;
  private final int[] positions;
  private final int mask;
  /** Hashes shared by more than one of the names, these are not in the hash table. */
  private final Set<Integer> clashHashes;
  /** A value that is not the hash of any of the names. */
  private final int unknownHash;

  JsonNames(String[] allNames, byte[][] nameArray, int[] hashes, int[] positions, Set<Integer> clashHashes) {
    this.allNames = allNames;
    this.nameArray = nameArray;
    this.rawArray = new byte[allNames.length][];
    final var allHashes = new HashSet<Integer>();
    for (int i = 0; i < allNames.length; i++) {
      rawArray[i] = allNames[i].getBytes(StandardCharsets.UTF_8);
      allHashes.add(JsonReader.nameHash(allNames[i]));
    }
    this.hashes = hashes;
    this.positions = positions;
    this.mask = positions.length - 1;
    this.clashHashes = clashHashes;
    int unknown = 0;
    while (allHashes.contains(unknown)) {
      unknown++;
    }
    this.unknownHash = unknown;
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
    return new JsonNames(names.clone(), nameArray, hashes, positions, Set.copyOf(clashKeys));
  }

  @Override
  public String name(int position) {
    return allNames[position];
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  /** Return the unescaped UTF-8 bytes of the name at the given position. */
  byte[] raw(int namePos) {
    return rawArray[namePos];
  }

  /** Return a value that is not the hash of any of the names. */
  int unknownHash() {
    return unknownHash;
  }

  /** Return true if the hash is shared by more than one of the names. */
  boolean isClash(int hash) {
    return !clashHashes.isEmpty() && clashHashes.contains(hash);
  }

  /**
   * Return the hash of the name when it is one of these names, otherwise a value that is not
   * the hash of any of these names.
   */
  int knownHash(String name) {
    final int hash = JsonReader.nameHash(name);
    for (String known : allNames) {
      if (known.equals(name)) {
        return hash;
      }
    }
    final int pos = position(hash);
    return pos >= 0 || clashHashes.contains(hash) ? unknownHash : hash;
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
