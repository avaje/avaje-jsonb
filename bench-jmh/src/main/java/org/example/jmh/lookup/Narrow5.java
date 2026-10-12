package org.example.jmh.lookup;

import io.avaje.jsonb.Json;

@Json
public record Narrow5(String id, String name, String email, String city, String country) {
}
