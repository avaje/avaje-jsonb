package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public record SimpleRecord(Integer size, String name) {
}
