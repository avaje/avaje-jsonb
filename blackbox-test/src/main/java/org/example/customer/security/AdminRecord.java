package org.example.customer.security;

import io.avaje.jsonb.Json;

@Json
public record AdminRecord(String name, boolean admin) {
}
