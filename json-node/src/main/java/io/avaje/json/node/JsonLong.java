package io.avaje.json.node;

import io.avaje.json.JsonWriter;

import java.math.BigDecimal;

public final class JsonLong implements JsonNumber {

  private final long value;

  public static JsonLong of(long value) {
    return new JsonLong(value);
  }

  private JsonLong(long value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return text();
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
    return (int)value;
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
