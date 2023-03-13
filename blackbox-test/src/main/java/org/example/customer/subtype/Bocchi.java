package org.example.customer.subtype;

public class Bocchi extends TheRock {

  private final int anxietyLvl;

  public Bocchi(long size, String name, int anxietyLvl) {
    super(size, name);
    this.anxietyLvl = anxietyLvl;
  }

  public int getAnxietyLvl() {
    return anxietyLvl;
  }
}
