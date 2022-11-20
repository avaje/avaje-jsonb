package org.example.customer.mixin;

public class CrewMate {

  private String c;
  private Integer susLv;
  private Integer taskNumber;

  public CrewMate(String c, Integer susLv, Integer taskNumber) {
    this.c = c;
    this.susLv = susLv;
    this.taskNumber = taskNumber;
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

  public Integer getTaskNumber() {
    return taskNumber;
  }

  public void setTaskNumber(Integer taskNumber) {
    this.taskNumber = taskNumber;
  }
}
