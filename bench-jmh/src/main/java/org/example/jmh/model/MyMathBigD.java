package org.example.jmh.model;

import io.avaje.jsonb.Json;

import java.math.BigDecimal;
import java.math.BigInteger;

@Json
public class MyMathBigD {

  BigDecimal one;

  BigDecimal two;

  public BigDecimal getOne() {
    return one;
  }

  public void setOne(BigDecimal one) {
    this.one = one;
  }

  public BigDecimal getTwo() {
    return two;
  }

  public void setTwo(BigDecimal two) {
    this.two = two;
  }
}
