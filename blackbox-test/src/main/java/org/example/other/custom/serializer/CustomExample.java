package org.example.other.custom.serializer;

import java.math.BigDecimal;

import io.avaje.jsonb.Json;

@Json
public record CustomExample(
    @Json.WithAdapter(MoneySerializer.class) BigDecimal amountOwed, BigDecimal somethingElse) {}
