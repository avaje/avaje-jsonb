package io.avaje.json.node;

import java.util.Objects;

public final /*value*/ class JsonString implements JsonNode {

  private static final long serialVersionUID = 1L;

  private final String value;

  public static JsonString of(String value) {
    return new JsonString(value);
  }

  private JsonString(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JsonString)) return false;
    JsonString that = (JsonString) object;
    return Objects.equals(value, that.value);
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
  public JsonString unmodifiable() {
    return this;
  }

  @Override
  public JsonString copy() {
    return this;
  }

  @Override
  public String toPlain() {
    return value;
  }

  @Override
  public Type type() {
    return Type.STRING;
  }

  @Override
  public String text() {
    return value;
  }

  public String value() {
    return value;
  }
}
