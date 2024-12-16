package io.avaje.json.node;

import io.avaje.json.JsonWriter;

import java.math.BigDecimal;
import java.util.Objects;

public final /*value*/ class JsonLong implements JsonNumber {

  private static final long serialVersionUID = 1L;

  private final long value;

  public static JsonLong of(long value) {
    return new JsonLong(value);
  }

  private JsonLong(long value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JsonLong)) return false;
    JsonLong jsonLong = (JsonLong) object;
    return value == jsonLong.value;
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
  public JsonLong unmodifiable() {
    return this;
  }

  @Override
  public JsonLong copy() {
    return this;
  }

  @Override
  public Long toPlain() {
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
