package io.avaje.jsonb.generator.models.valid;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public class MultiConstruct {

  private final List<String> body;
  private String setter;

  public MultiConstruct(String string) {
    this(List.of(string));
  }

  public MultiConstruct(List<String> body) {
    this.body = body;
  }

  public MultiConstruct(List<String> body, int somethin) {
    this.body = body;
  }

  public String getSetter() {
    return setter;
  }

  public void setSetter(String setter) {
    this.setter = setter;
  }

  public List<String> getBody() {
    return body;
  }
}
