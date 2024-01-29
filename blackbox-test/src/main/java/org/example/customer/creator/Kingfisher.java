package org.example.customer.creator;

public class Kingfisher {
  private final String name;
  private int fishCaught;

  public Kingfisher(String name) {
    this.name = name;
  }

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
