package org.example.customer.subtype;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = InheritanceSplitProperty.NestedTest.class)
public interface InheritanceSplitProperty {
  @Json.Property("value")
  int value();

  public static class NestedTest implements InheritanceSplitProperty {
    private int value;

    NestedTest(int value) {
      this.value = value;
    }

    @Override
    public int value() {
      return this.value;
    }
  }
}
