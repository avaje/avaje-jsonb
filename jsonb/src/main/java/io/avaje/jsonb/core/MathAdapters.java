package io.avaje.jsonb.core;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

class MathAdapters implements JsonAdapter.Factory {

  private final JsonAdapter<BigDecimal> decimal;
  private final JsonAdapter<BigInteger> bigint;

  MathAdapters(boolean asString) {
    decimal = asString ? new DecimalString().nullSafe() : new DecimalNumber().nullSafe();
    bigint = asString ? new BigIntString().nullSafe() : new BigIntNumber().nullSafe();
  }

  @Override
  public JsonAdapter<?> create(Type type, Jsonb jsonb) {
    if (type == BigDecimal.class) return decimal;
    if (type == BigInteger.class) return bigint;
    return null;
  }

  private static final class DecimalString extends JsonAdapter<BigDecimal> {
    @Override
    public BigDecimal fromJson(JsonReader reader) throws IOException {
      return new BigDecimal(reader.nextString());
    }

    @Override
    public void toJson(JsonWriter writer, BigDecimal value) throws IOException {
      writer.rawValue("\"" + value.toString() + "\"");
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigDecimal:String)";
    }
  }

  private static final class DecimalNumber extends JsonAdapter<BigDecimal> {
    @Override
    public BigDecimal fromJson(JsonReader reader) throws IOException {
      return reader.nextDecimal();
    }

    @Override
    public void toJson(JsonWriter writer, BigDecimal value) throws IOException {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigDecimal:Double)";
    }
  }

  private static final class BigIntString extends JsonAdapter<BigInteger> {
    @Override
    public BigInteger fromJson(JsonReader reader) throws IOException {
      return new BigInteger(reader.nextString());
    }

    @Override
    public void toJson(JsonWriter writer, BigInteger value) throws IOException {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigInteger:String)";
    }
  }

  private static final class BigIntNumber extends JsonAdapter<BigInteger> {
    @Override
    public BigInteger fromJson(JsonReader reader) throws IOException {
      return reader.nextBigInteger();
    }

    @Override
    public void toJson(JsonWriter writer, BigInteger value) throws IOException {
      writer.rawValue(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigInteger)";
    }
  }

}
