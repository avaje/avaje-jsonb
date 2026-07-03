package io.avaje.jsonb.generator.models.valid.subtype.subpackage;

import io.avaje.jsonb.generator.models.valid.subtype.ImportedBase;

public class Imported {
  public static class Subtype implements ImportedBase {
    @Override
    public String getName() {
      return "Subtype of Imported";
    }
  }
}
