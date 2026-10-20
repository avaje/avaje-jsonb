package org.example.customer.security;

import io.avaje.jsonb.Json;

import java.util.Map;

@Json
public record AdminUnmapped(String name, boolean admin, @Json.Unmapped Map<String, Object> unmapped) {
}
