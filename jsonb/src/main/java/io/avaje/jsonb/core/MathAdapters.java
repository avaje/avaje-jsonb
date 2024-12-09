package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

final class MathAdapters implements AdapterFactory {

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

  private static final class DecimalString implements JsonAdapter<BigDecimal> {
    @Override
    public BigDecimal fromJson(JsonReader reader) {
      return new BigDecimal(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, BigDecimal value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigDecimal:String)";
    }
  }

  private static final class DecimalNumber implements JsonAdapter<BigDecimal> {
    @Override
    public BigDecimal fromJson(JsonReader reader) {
      return reader.readDecimal();
    }

    @Override
    public void toJson(JsonWriter writer, BigDecimal value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigDecimal:Double)";
    }
  }

  private static final class BigIntString implements JsonAdapter<BigInteger> {
    @Override
    public BigInteger fromJson(JsonReader reader) {
      return new BigInteger(reader.readString());
    }

    @Override
    public void toJson(JsonWriter writer, BigInteger value) {
      writer.value(value.toString());
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigInteger:String)";
    }
  }

  private static final class BigIntNumber implements JsonAdapter<BigInteger> {
    @Override
    public BigInteger fromJson(JsonReader reader) {
      return reader.readBigInteger();
    }

    @Override
    public void toJson(JsonWriter writer, BigInteger value) {
      writer.value(value);
    }

    @Override
    public String toString() {
      return "JsonAdapter(BigInteger)";
    }
  }

}
