package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.io.SerializedString;
import io.avaje.json.JsonReader;
import io.avaje.json.PropertyNames;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class JacksonNames implements PropertyNames {

  static final JacksonNames EMPTY = new JacksonNames(new String[0]);

  private static final NameCache NAME_CACHE = new NameCache();

  private final SerializedString[] keys;
  private final Map<Integer, String> uniqueNames;
  private final Set<Integer> clashHashes;
  private final int unknownHash;

  JacksonNames(String[] names) {
    keys = new SerializedString[names.length];
    final var uniqueNames = new HashMap<Integer, String>();
    final var clashHashes = new HashSet<Integer>();
    final var allHashes = new HashSet<Integer>();
    for (int i = 0; i < names.length; i++) {
      keys[i] = obtain(names[i]);
      final int hash = JsonReader.nameHash(names[i]);
      allHashes.add(hash);
      if (!clashHashes.contains(hash)) {
        final String priorName = uniqueNames.putIfAbsent(hash, names[i]);
        if (priorName != null && !priorName.equals(names[i])) {
          uniqueNames.remove(hash);
          clashHashes.add(hash);
        }
      }
    }
    this.uniqueNames = Map.copyOf(uniqueNames);
    this.clashHashes = Set.copyOf(clashHashes);
    int unknown = 0;
    while (allHashes.contains(unknown)) {
      unknown++;
    }
    this.unknownHash = unknown;
  }

  SerializedString key(int pos) {
    return keys[pos];
  }

  @Override
  public String name(int position) {
    return keys[position].getValue();
  }

  @Override
  public int knownHash(String name) {
    final int hash = JsonReader.nameHash(name);
    if (clashHashes.contains(hash)) {
      for (SerializedString key : keys) {
        if (key.getValue().equals(name)) {
          return hash;
        }
      }
      return unknownHash;
    }
    final String known = uniqueNames.get(hash);
    if (known != null) {
      return known.equals(name) ? hash : unknownHash;
    }
    return hash;
  }

  private static SerializedString obtain(String name) {
    return NAME_CACHE.get(name);
  }
}
