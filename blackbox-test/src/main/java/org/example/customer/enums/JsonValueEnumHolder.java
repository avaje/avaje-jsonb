package org.example.customer.enums;

import java.util.List;

import io.avaje.jsonb.Json;

/**
 * Wraps {@link JsonValueEnum} as a single field and as a list field, to
 * verify the enum works correctly when used inside another {@code @Json}
 * type (not just as a raw/top level type).
 */
@Json
public record JsonValueEnumHolder(String name, JsonValueEnum value, List<JsonValueEnum> values) {}
