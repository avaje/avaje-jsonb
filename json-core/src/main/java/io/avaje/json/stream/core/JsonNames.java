package io.avaje.json.stream.core;

import io.avaje.json.PropertyNames;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Provides "prepared" JSON keys/field names for improved performance
 * during generation and parsing.
 */
final class JsonNames implements PropertyNames {

  static final JsonNames EMPTY = new JsonNames(new byte[0][0], Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  private final byte[][] nameArray;
  private final Map<Integer, String> nameHash;
  /** Raw (unescaped) UTF-8 bytes of each name, keyed by hash, used to confirm a hash match. */
  private final Map<Integer, byte[]> rawNameHash;
  /** Canonical quoted+escaped bytes of each name (as written in JSON), keyed by hash. */
  private final Map<Integer, byte[]> quotedNameHash;

  JsonNames(byte[][] nameArray, Map<Integer, String> nameHash, Map<Integer, byte[]> rawNameHash, Map<Integer, byte[]> quotedNameHash) {
    this.nameArray = nameArray;
    this.nameHash = nameHash;
    this.rawNameHash = rawNameHash;
    this.quotedNameHash = quotedNameHash;
  }

  /**
   * Create given the names.
   */
  public static JsonNames of(String... names) {
    final Set<Integer> clashKeys = new HashSet<>();
    final Map<Integer, String> nameHash = new HashMap<>();
    final Map<Integer, byte[]> rawNameHash = new HashMap<>();
    final Map<Integer, byte[]> quotedNameHash = new HashMap<>();
    final byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      final byte[] quoted = Escape.quoteEscape(names[i]);
      nameArray[i] = quoted;
      final int hash = Escape.nameHash(names[i]);
      final String priorKey = nameHash.put(hash, names[i]);
      rawNameHash.put(hash, names[i].getBytes(StandardCharsets.UTF_8));
      quotedNameHash.put(hash, quoted);
      if (priorKey != null && !priorKey.equals(names[i])) {
        clashKeys.add(hash);
      }
    }
    for (Integer clashKey : clashKeys) {
      nameHash.remove(clashKey);
      rawNameHash.remove(clashKey);
      quotedNameHash.remove(clashKey);
    }
    return new JsonNames(nameArray, nameHash, rawNameHash, quotedNameHash);
  }

  byte[] key(int namePos) {
    return nameArray[namePos];
  }

  String lookup(int hash) {
     return nameHash.get(hash);
  }

  /**
   * Return the raw (unescaped) UTF-8 bytes of the name registered for the given hash,
   * or {@code null} when no name has that hash. Used to confirm a hash match against the
   * copied name bytes (streaming cross-buffer path).
   */
  byte[] rawName(int hash) {
    return rawNameHash.get(hash);
  }

  /**
   * Return the canonical quoted+escaped bytes (including the surrounding quotes) of the name
   * registered for the given hash, or {@code null} when none. Used to confirm a hash match
   * against the literal in-buffer key bytes so that a colliding key is rejected.
   */
  byte[] quotedName(int hash) {
    return quotedNameHash.get(hash);
  }
}
