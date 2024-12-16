package io.avaje.json.node;

import io.avaje.json.JsonWriter;

import java.math.BigDecimal;
import java.util.Objects;

public final /*value*/ class JsonDouble implements JsonNumber {

  private static final long serialVersionUID = 1L;

  private final double value;

  public static JsonDouble of(double value) {
    return new JsonDouble(value);
  }

  private JsonDouble(double value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof JsonDouble)) return false;
    JsonDouble that = (JsonDouble) object;
    return Double.compare(value, that.value) == 0;
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
  public JsonDouble unmodifiable() {
    return this;
  }

  @Override
  public JsonDouble copy() {
    return this;
  }

  @Override
  public Double toPlain() {
    return value;
  }

  @Override
  public Type type() {
    return Type.NUMBER;
  }

  @Override
  public String text() {
    return Double.toString(value);
  }

  @Override
  public int intValue() {
    return (int)value;
  }

  @Override
  public long longValue() {
    return (long)value;
  }

  @Override
  public double doubleValue() {
    return value;
  }

  @Override
  public BigDecimal decimalValue() {
    return BigDecimal.valueOf(this.value);
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
