package org.example.jmh.model;

import io.avaje.jsonb.Json;

import java.math.BigDecimal;
import java.math.BigInteger;

@Json
public class MyMathBigInt {

  BigInteger one;

  BigInteger two;

  public BigInteger getOne() {
    return one;
  }

  public void setOne(BigInteger one) {
    this.one = one;
  }

  public BigInteger getTwo() {
    return two;
  }

  public void setTwo(BigInteger two) {
    this.two = two;
  }
}
