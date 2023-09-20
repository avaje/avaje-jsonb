package io.avaje.jsonb.generator.models.valid;

import io.avaje.jsonb.Json;

@Json.Import(MyThrowable.class)
public class MyThrowable extends IllegalArgumentException {

  public MyThrowable(String message) {
    super(message);
  }
  public MyThrowable(String message, IllegalArgumentException cause) {
    super(message, cause);
  }
}
