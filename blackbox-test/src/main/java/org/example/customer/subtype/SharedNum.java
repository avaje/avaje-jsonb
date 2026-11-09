package org.example.customer.subtype;

public class SharedNum extends Shared {

  private final int value;

  public SharedNum(String id, int value) {
    super(id);
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
