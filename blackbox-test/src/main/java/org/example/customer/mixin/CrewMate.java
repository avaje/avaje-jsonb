package org.example.customer.mixin;

public class CrewMate {

  private String c;
  private Integer susLv;

  public CrewMate(String c, Integer susLv) {
    this.c = c;
    this.susLv = susLv;
  }

  public String getC() {
    return c;
  }

  public void setC(String c) {
    this.c = c;
  }

  public Integer getSusLv() {
    return susLv;
  }

  public void setSusLv(Integer susLv) {
    this.susLv = susLv;
  }
}
