package org.example.jmh.model;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.math.BigDecimal;
import java.math.BigInteger;

@CompiledJson
public class MyDslData {

  @JsonAttribute(name = "\"foo\"")
  boolean abool;
  int aint;
  long along;
  String astr;

  BigDecimal decimal;
  BigInteger bigint;

  public boolean isAbool() {
    return abool;
  }

  public void setAbool(boolean abool) {
    this.abool = abool;
  }

  public int getAint() {
    return aint;
  }

  public void setAint(int aint) {
    this.aint = aint;
  }

  public long getAlong() {
    return along;
  }

  public void setAlong(long along) {
    this.along = along;
  }

  public String getAstr() {
    return astr;
  }

  public void setAstr(String astr) {
    this.astr = astr;
  }

  public BigDecimal getDecimal() {
    return decimal;
  }

  public void setDecimal(BigDecimal decimal) {
    this.decimal = decimal;
  }

  public BigInteger getBigint() {
    return bigint;
  }

  public void setBigint(BigInteger bigint) {
    this.bigint = bigint;
  }
}
