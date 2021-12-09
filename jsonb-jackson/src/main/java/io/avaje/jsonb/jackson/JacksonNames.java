package io.avaje.jsonb.jackson;

import com.fasterxml.jackson.core.io.SerializedString;
import io.avaje.jsonb.spi.PropertyNames;

final class JacksonNames implements PropertyNames {

  private static final NameCache NAME_CACHE = new NameCache();

  private final SerializedString[] keys;

  JacksonNames(String[] names) {
    keys = new SerializedString[names.length];
    for (int i = 0; i < names.length; i++) {
      keys[i] = obtain(names[i]);
    }
  }

  SerializedString key(int pos) {
    return keys[pos];
  }

  private static SerializedString obtain(String name) {
    return NAME_CACHE.get(name);
  }
}
