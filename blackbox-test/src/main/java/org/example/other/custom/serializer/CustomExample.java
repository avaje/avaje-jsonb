package org.example.other.custom.serializer;

import java.math.BigDecimal;

import io.avaje.jsonb.Json;

@Json
public record CustomExample(
    @Json.Serializer(MoneySerializer.class)
    BigDecimal amountOwed,
    BigDecimal somethingElse) {}
