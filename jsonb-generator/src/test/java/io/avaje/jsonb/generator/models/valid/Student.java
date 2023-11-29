package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json
public class Student {
  private final String name;
  private int rollNo;

  @Json.Creator
  public Student(@Json.Alias("theName") String name, long rolling) {
    this.name = name;
    this.rollNo = name.length();
  }

  public Student(String name, int rollNo) {
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
