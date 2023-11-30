package org.example.customer.creator;

import io.avaje.jsonb.Json;

@Json
public class StudentViaStaticMethod {
  private final String name;
  private int rollNo;

  @Json.Creator
  public static StudentViaStaticMethod create(@Json.Alias("theName") String name, long rolling) {
    return new StudentViaStaticMethod(name, name.length());
  }

  public StudentViaStaticMethod(String name, int rollNo) {
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
