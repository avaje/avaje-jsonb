package io.avaje.jsonb.diesel;

import io.avaje.jsonb.spi.PropertyNames;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class JsonNames implements PropertyNames {

  private final byte[][] nameArray;
  private final Map<Integer, String> nameHash;

  JsonNames(byte[][] nameArray, Map<Integer, String> nameHash) {
    this.nameArray = nameArray;
    this.nameHash = nameHash;
  }

  static JsonNames of(String... names) {
    boolean hashingClash = false;
    Map<Integer, String> nameHash = new HashMap<>();
    byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
      int hash = Escape.nameHash(names[i]);
      if (nameHash.put(hash, names[i]) != null) {
        hashingClash = true;
      }
    }
    return  new JsonNames(nameArray, hashingClash ? Collections.emptyMap() : nameHash);
  }

  public byte[] key(int namePos) {
    return nameArray[namePos];
  }

  public String lookup(int hash) {
    return nameHash.get(hash);
  }
}
