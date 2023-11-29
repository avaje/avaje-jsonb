package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;


public class KingFisher {
  private String name;
  private int fishCaught;


  public String getName() {
    return name;
  }

  public int getFishCaught() {
    return fishCaught;
  }

  public void setFishCaught(int fishCaught) {
    this.fishCaught = fishCaught;
  }
}
