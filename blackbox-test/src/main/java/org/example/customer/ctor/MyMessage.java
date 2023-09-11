package org.example.customer.ctor;


import io.avaje.jsonb.Json;

@Json
public class MyMessage {

  protected String path;
  protected String field;
  protected String message;

  public MyMessage(String path, String field, String message) {
    this.path = path;
    this.field = field;
    this.message = message;
  }

  /** Default constructor typically to help Jackson onlu. */
  public MyMessage() {
  }

  public String getPath() {
    return path;
  }

  public String getField() {
    return field;
  }

  public String getMessage() {
    return message;
  }
}
