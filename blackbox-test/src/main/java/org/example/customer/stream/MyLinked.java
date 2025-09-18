package org.example.customer.stream;

import io.avaje.jsonb.Json;

import java.util.LinkedHashSet;

@Json
public record MyLinked(int id, LinkedHashSet<String> names) {
}
