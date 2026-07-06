package org.example.customer.enums;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public record PlainJsonEnumHolder(String name, PlainJsonEnum value, List<PlainJsonEnum> values) {}
