package io.avaje.json.stream.core;

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
  /** The names as written for a field, with a leading comma and trailing colon. */
  private final byte[][] fieldArray;
  /** For a short field, {@code "name":} as 2 little endian longs (zero padded), else null. */
  private final long[] shortFields;
  /** Open addressing table of name hash to name position, -1 for an empty slot. */
  private final int[] hashes;
  private final int[] positions;
  private final int mask;
  /** Hashes shared by more than one of the names, these are not in the hash table. */
  private final Set<Integer> clashHashes;
  /** A value that is not the hash of any of the names. */
  private final int unknownHash;

  JsonNames(String[] allNames, byte[][] nameArray, byte[][] fieldArray, int[] hashes, int[] positions, Set<Integer> clashHashes) {
    this.allNames = allNames;
    this.nameArray = nameArray;
    this.fieldArray = fieldArray;
    this.shortFields = shortFields(fieldArray);
    final var allHashes = new HashSet<Integer>();
    for (int i = 0; i < allNames.length; i++) {
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
    final byte[][] fieldArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
      fieldArray[i] = field(nameArray[i]);
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
    return new JsonNames(names.clone(), nameArray, fieldArray, hashes, positions, Set.copyOf(clashKeys));
  }

  private static long[] shortFields(byte[][] fieldArray) {
    final long[] longs = new long[fieldArray.length * 2];
    for (int i = 0; i < fieldArray.length; i++) {
      final byte[] field = fieldArray[i];
      if (field.length - 1 > 16) {
        return null;
      }
      for (int b = 1; b < field.length; b++) {
        longs[i * 2 + (b - 1) / 8] |= (field[b] & 0xFFL) << (((b - 1) % 8) * 8);
      }
    }
    return longs;
  }

  /** Return the short fields as longs, null when any {@code "name":} is longer than 16 bytes. */
  long[] shortFields() {
    return shortFields;
  }

  private static byte[] field(byte[] quoted) {
    final byte[] field = new byte[quoted.length + 2];
    field[0] = ',';
    System.arraycopy(quoted, 0, field, 1, quoted.length);
    field[field.length - 1] = ':';
    return field;
  }

  @Override
  public String name(int position) {
    return allNames[position];
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  /** Return the name as written for a field, {@code ,"name":} */
  byte[] field(int namePos) {
    return fieldArray[namePos];
  }

  /** Return a value that is not the hash of any of the names. */
  int unknownHash() {
    return unknownHash;
  }

  /** Return true if the hash is shared by more than one of the names. */
  boolean isClash(int hash) {
    return !clashHashes.isEmpty() && clashHashes.contains(hash);
  }

  @Override
  public int knownHash(String name) {
    final int hash = JsonReader.nameHash(name);
    final int pos = position(hash);
    if (pos >= 0) {
      return allNames[pos].equals(name) ? hash : unknownHash;
    }
    if (isClash(hash)) {
      for (String known : allNames) {
        if (known.equals(name)) {
          return hash;
        }
      }
      return unknownHash;
    }
    return hash;
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
