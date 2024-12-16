package io.avaje.json.node;

import java.util.Objects;

public final /*value*/ class JsonBoolean implements JsonNode {

  private static final long serialVersionUID = 1L;

  private final boolean value;

  public static JsonBoolean of(boolean value) {
    return new JsonBoolean(value);
  }

  private JsonBoolean(boolean value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JsonBoolean)) return false;
    JsonBoolean that = (JsonBoolean) object;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return text();
  }

  @Override
  public JsonBoolean unmodifiable() {
    return this;
  }

  @Override
  public JsonBoolean copy() {
    return this;
  }

  @Override
  public Boolean toPlain() {
    return value;
  }

  @Override
  public Type type() {
    return Type.BOOLEAN;
  }

  @Override
  public String text() {
    return Boolean.toString(value);
  }

  public boolean value() {
    return value;
  }

}
