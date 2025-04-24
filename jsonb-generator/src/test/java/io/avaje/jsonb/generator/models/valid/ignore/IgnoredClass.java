package io.avaje.jsonb.generator.models.valid.ignore;

import java.math.BigDecimal;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Ignore;

@Json
public class IgnoredClass {

  @Json.Property("a")
  BigDecimal amount;

  BigDecimal amountReturned;

  @Ignore(deserialize = true)
  BigDecimal somethingElse;
}
