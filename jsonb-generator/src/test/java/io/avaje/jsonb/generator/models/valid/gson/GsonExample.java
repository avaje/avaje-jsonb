package io.avaje.jsonb.generator.models.valid.gson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.avaje.jsonb.Json;

@Json.Import(GsonExample.class)
public class GsonExample {

  @SerializedName(value = "id", alternate = {"identifier", "userId"})
  private final int id;

  @SerializedName("name")
  private String name;

  @Expose(serialize = false)
  private String internalCode;

  public GsonExample(int id, String name) {
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
