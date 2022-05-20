package org.example.customer.customtype;

import java.util.Arrays;
import java.util.Base64;

public final class MyCustomScalarType {

  private final byte[] data;

  public MyCustomScalarType(byte[] data) {
    this.data = data;
  }

  public static MyCustomScalarType of(String encoded) {
    byte[] bytes = Base64.getDecoder().decode(encoded);
    return new MyCustomScalarType(bytes);
  }

  public static MyCustomScalarType of(byte[] raw) {
    return new MyCustomScalarType(raw);
  }

  @Override
  public String toString() {
    return Base64.getEncoder().encodeToString(data);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MyCustomScalarType that = (MyCustomScalarType) o;
    return Arrays.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }
}
