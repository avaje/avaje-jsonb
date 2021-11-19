package org.example.jmh.model;

import io.avaje.jsonb.Json;

import java.math.BigDecimal;
import java.math.BigInteger;

@Json
public class MyMathTypes {

  BigDecimal decimal;

  BigInteger bigInt;

  public BigDecimal getDecimal() {
    return decimal;
  }

  public void setDecimal(BigDecimal decimal) {
    this.decimal = decimal;
  }

  public BigInteger getBigInt() {
    return bigInt;
  }

  public void setBigInt(BigInteger bigInt) {
    this.bigInt = bigInt;
  }
}
