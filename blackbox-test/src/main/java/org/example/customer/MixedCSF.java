package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public class MixedCSF {

  // constructor
  private final String one;
  // setters
  private Long two;
  private Long three;
  // public field
  public Long four;

  public int[] flags;

  public MixedCSF(String one) {
    this.one = one;
  }

  public String one() {
    return one;
  }

  public Long getTwo() {
    return two;
  }

  public void two(Long two) {
    this.two = two;
  }

  public Long getThree() {
    return three;
  }

  public MixedCSF setThree(Long three) {
    this.three = three;
    return this;
  }

}
