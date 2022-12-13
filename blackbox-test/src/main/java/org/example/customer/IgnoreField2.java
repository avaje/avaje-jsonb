package org.example.customer;

import io.avaje.jsonb.Json;

import java.time.Instant;

@Json
public class IgnoreField2 {

  @Json.Ignore
  private String middleName;
  private Instant time;
  private int num;

  public String getMiddleName() {
    return middleName;
  }

  public IgnoreField2 setMiddleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public Instant getTime() {
    return time;
  }

  public IgnoreField2 setTime(Instant time) {
    this.time = time;
    return this;
  }

  public int getNum() {
    return num;
  }

  public IgnoreField2 setNum(int num) {
    this.num = num;
    return this;
  }
}
