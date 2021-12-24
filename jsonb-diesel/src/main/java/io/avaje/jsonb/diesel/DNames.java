package io.avaje.jsonb.diesel;

import io.avaje.jsonb.spi.PropertyNames;

public final class DNames implements PropertyNames {

  private final byte[][] nameArray;

  DNames(byte[][] nameArray) {
    this.nameArray = nameArray;
  }

  public static DNames of(String... names) {

    byte[][] nameArray = new byte[names.length][];
    for (int i = 0; i < names.length; i++) {
      nameArray[i] = Escape.quoteEscape(names[i]);
    }
    return  new DNames(nameArray);
  }

  public byte[] key(int namePos) {
    return nameArray[namePos];
  }
}
