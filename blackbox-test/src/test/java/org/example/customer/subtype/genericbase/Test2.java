package org.example.customer.subtype.genericbase;

import io.avaje.jsonb.Json;

@Json
public class Test2 extends Base<Test2.Config> {

  public Test2(Config config) {
    super(config);
  }

  @Json
  public static class Config {
    private String a;

    public Config() {}

    public Config(String a) {
      this.a = a;
    }

    public String getA() {
      return a;
    }
  }
}
