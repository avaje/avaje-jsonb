package io.avaje.jsonb.generator.models.valid.jackson;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.avaje.jsonb.Json;

@Json.Import(JacksonExample.class)
public class JacksonExample {

  @JsonProperty("id")
  private final int id;

  @JsonProperty("name")
  private String name;

  @JsonIgnore private String internalCode;

  @JsonCreator
  public JacksonExample(
      @JsonAlias({"identifier", "userId"}) @JsonProperty("id") int id,
      @JsonProperty("name") String name) {
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
