package io.avaje.json.node;

import io.avaje.json.JsonWriter;

import java.math.BigDecimal;
import java.util.Objects;

public final /*value*/ class JsonInteger implements JsonNumber {

  private static final long serialVersionUID = 1L;

  private final int value;

  public static JsonInteger of(int value) {
    return new JsonInteger(value);
  }

  private JsonInteger(int value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JsonInteger)) return false;
    JsonInteger that = (JsonInteger) object;
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
  public JsonInteger unmodifiable() {
    return this;
  }

  @Override
  public JsonInteger copy() {
    return this;
  }

  @Override
  public Integer toPlain() {
    return value;
  }

  @Override
  public Type type() {
    return Type.NUMBER;
  }

  @Override
  public String text() {
    return Long.toString(value);
  }

  @Override
  public int intValue() {
    return value;
  }

  @Override
  public long longValue() {
    return value;
  }

  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public BigDecimal decimalValue() {
    return BigDecimal.valueOf(value);
  }

  @Override
  public Number numberValue() {
    return value;
  }

  @Override
  public void toJson(JsonWriter writer) {
    writer.value(value);
  }
}
