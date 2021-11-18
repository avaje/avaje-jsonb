package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.io.SerializedString;
import io.avaje.jsonb.spi.MetaNames;

final class JacksonNames implements MetaNames {

  private final SerializedString[] keys;

  JacksonNames(String[] names) {
    keys = new SerializedString[names.length];
    for (int i = 0; i < names.length; i++) {
      keys[i] = keyOf(names[i]);
    }
  }

  SerializedString key(int pos) {
    return keys[pos];
  }

  private SerializedString keyOf(String name) {
    SerializedString key = new SerializedString(name);
    key.asQuotedChars();
    return key;
  }
}
