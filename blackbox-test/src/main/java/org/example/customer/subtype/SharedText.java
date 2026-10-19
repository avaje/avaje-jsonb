package org.example.customer.subtype;

public class SharedText extends Shared {

  private final String value;

  public SharedText(String id, String value) {
    super(id);
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
