package org.example.other.custom.serializer;

import io.avaje.jsonb.Json;

import java.math.BigDecimal;

@Json
public record CustomExample2(
    @Json.Serializer(MoneySerializer2.class)
    BigDecimal amountOwed,
    BigDecimal somethingElse) {}
