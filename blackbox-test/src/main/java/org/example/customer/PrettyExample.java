package org.example.customer;

import java.util.List;

import io.avaje.jsonb.Json;

@Json
public record PrettyExample(String name, List<String> values, int count) {}
