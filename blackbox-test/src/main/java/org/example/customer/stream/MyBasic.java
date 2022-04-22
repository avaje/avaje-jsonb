package org.example.customer.stream;

import io.avaje.jsonb.Json;

@Json
public record MyBasic(int id, String name) {
}
