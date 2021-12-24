package io.avaje.jsonb.diesel;

import io.avaje.jsonb.spi.PropertyNames;

final class JsonNames implements PropertyNames {

  private final byte[][] nameArray;

  JsonNames(byte[][] nameArray) {
    this.nameArray = nameArray;
  }

  static JsonNames of(String... names) {
    byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
    }
    return  new JsonNames(nameArray);
  }

  public byte[] key(int namePos) {
    return nameArray[namePos];
  }
}
