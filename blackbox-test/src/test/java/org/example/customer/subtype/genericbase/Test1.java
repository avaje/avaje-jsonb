package org.example.customer.subtype.genericbase;

import io.avaje.jsonb.Json;

@Json
public class Test1 extends Base<Test1.Config> {

  public Test1(Config config) {
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
