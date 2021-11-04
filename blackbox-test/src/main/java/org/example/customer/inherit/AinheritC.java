package org.example.customer.inherit;

import io.avaje.jsonb.Json;

@Json
public class AinheritC extends AinheritB {

  private final String levelC;

  public AinheritC(String levelC) {
    this.levelC = levelC;
  }

  public String levelC() {
    return levelC;
  }
}
