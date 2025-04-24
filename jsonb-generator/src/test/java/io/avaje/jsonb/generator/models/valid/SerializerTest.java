package io.avaje.jsonb.generator.models.valid;

import java.math.BigDecimal;

import io.avaje.jsonb.Json;

@Json
@Json.Ignore
public class SerializerTest {

  @Json.Property(value = "")
  BigDecimal amount;

  @Json.Serializer(MoneySerializer.class)
  BigDecimal amountReturned;

  BigDecimal somethingElse;

  @Json.Property("methodCustom")
  @Json.Serializer(MoneySerializer.class)
  BigDecimal methodCustom() {
    return null;
  }
}
