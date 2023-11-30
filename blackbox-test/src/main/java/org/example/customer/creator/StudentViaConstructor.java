package org.example.customer.creator;

import io.avaje.jsonb.Json;

@Json
public class StudentViaConstructor {
  private final String name;
  private int rollNo;

  @Json.Creator
  public StudentViaConstructor(@Json.Alias("theName") String name, long rolling) {
    this.name = name;
    this.rollNo = name.length();
  }

  public StudentViaConstructor(String name, int rollNo) {
    this.name = name;
    this.rollNo = rollNo;
  }

  public String getName() {
    return name;
  }

  public int getRollNo() {
    return rollNo;
  }

  public void setRollNo(int rollNo) {
    this.rollNo = rollNo;
  }
}
