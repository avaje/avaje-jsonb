package org.example.customer.inherit;

import io.avaje.jsonb.Json;

@Json
public class ConcreteWithGenericBase extends GenericBase<ConcreteWithGenericBase.InnerConfig> {

  public ConcreteWithGenericBase(InnerConfig config) {
    super(config);
  }

  public static class InnerConfig {
    private String value;

    public InnerConfig() {}

    public InnerConfig(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
