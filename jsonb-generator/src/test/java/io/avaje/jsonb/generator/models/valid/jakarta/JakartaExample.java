package io.avaje.jsonb.generator.models.valid.jakarta;

import io.avaje.jsonb.Json;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

@Json.Import(JakartaExample.class)
public class JakartaExample {

  @JsonbProperty("id")
  private final int id;

  @JsonbProperty("name")
  private String name;

  @JsonbTransient
  private String internalCode;

  public JakartaExample(int id, String name) {
    this.id = id;
    this.name = name;
    this.internalCode = "";
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getInternalCode() {
    return internalCode;
  }

  public void setInternalCode(String internalCode) {
    this.internalCode = internalCode;
  }
}
