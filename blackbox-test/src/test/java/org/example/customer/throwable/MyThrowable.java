package org.example.customer.throwable;

public class MyThrowable extends IllegalArgumentException {

  public MyThrowable(String message) {
    super(message);
  }
  public MyThrowable(String message, IllegalArgumentException cause) {
    super(message, cause);
  }
}
