package io.avaje.json.node;

import io.avaje.json.JsonWriter;

import java.math.BigDecimal;

public final /*value*/ class JsonDecimal implements JsonNumber {

  private final BigDecimal value;

  public static JsonDecimal of(BigDecimal value) {
    return new JsonDecimal(value);
  }

  private JsonDecimal(BigDecimal value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return text();
  }

  @Override
  public JsonDecimal unmodifiable() {
    return this;
  }

  @Override
  public JsonDecimal copy() {
    return this;
  }

  @Override
  public Type type() {
    return Type.NUMBER;
  }

  @Override
  public String text() {
    return value.toString();
  }

  @Override
  public int intValue() {
    return value.intValueExact();
  }

  @Override
  public long longValue() {
    return value.longValueExact();
  }

  @Override
  public double doubleValue() {
    return value.doubleValue();
  }

  @Override
  public BigDecimal decimalValue() {
    return value;
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
