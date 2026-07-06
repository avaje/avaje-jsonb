package org.example.customer.subtype.subpackage;

import org.example.customer.subtype.ImportedBase;

public class Imported {
  public static class Subtype implements ImportedBase {
    private String name = "Subtype of Imported";

    @Override
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
