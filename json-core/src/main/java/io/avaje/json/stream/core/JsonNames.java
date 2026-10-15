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
  private final Map<Integer, Entry> hashEntry;

  /**
   * A name and its quoted, escaped bytes, found by the name hash.
   */
  static final class Entry {
    private final String name;
    private final byte[] quoted;

    Entry(String name, byte[] quoted) {
      this.name = name;
      this.quoted = quoted;
    }

    String name() {
      return name;
    }

    byte[] quoted() {
      return quoted;
    }
  }

  JsonNames(byte[][] nameArray, Map<Integer, Entry> hashEntry) {
    this.nameArray = nameArray;
    this.hashEntry = hashEntry;
  }

  /**
   * Create given the names.
   */
  public static JsonNames of(String... names) {
    final Set<Integer> clashKeys = new HashSet<>();
    final Map<Integer, Entry> hashEntry = new HashMap<>();
    final byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
      final int hash = Escape.nameHash(names[i]);
      final Entry prior = hashEntry.put(hash, new Entry(names[i], nameArray[i]));
      if (prior != null && !prior.name().equals(names[i])) {
        clashKeys.add(hash);
      }
    }
    for (Integer clashKey : clashKeys) {
      hashEntry.remove(clashKey);
    }
    return new JsonNames(nameArray, hashEntry);
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  /**
   * Return the entry with the given hash, or null when there is none. A hash match is only a
   * candidate, the caller must confirm the actual key against the entry.
   */
  Entry find(int hash) {
    return hashEntry.get(hash);
  }

  String lookup(int hash) {
    final Entry entry = find(hash);
    return entry == null ? null : entry.name();
  }
}
