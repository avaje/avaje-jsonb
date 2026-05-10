package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json
@Json.SubType(type = PropertyInterface.NestedTest.class, name = "PropertyInterface.NestedTest")
public interface PropertyInterface {

  @Json.Property("methodOnly")
  String methodOnly();

  class NestedTest implements PropertyInterface {

    public NestedTest() {}

    @Override
    public String methodOnly() {
      return "foo";
    }
  }
}
