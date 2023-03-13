package org.example.customer.subtype;

public class Dwayne extends TheRock {

  private final int cash;

  public Dwayne(long size, String name, int cash) {
    super(size, name);
    this.cash = cash;
  }

  public int getCash() {
    return cash;
  }
}
