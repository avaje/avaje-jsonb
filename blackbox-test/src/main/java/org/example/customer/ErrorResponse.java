package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public record ErrorResponse(String id, String text) {}
