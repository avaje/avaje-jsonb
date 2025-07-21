package org.example.other;

import io.avaje.jsonb.Json;

@Json
public record MyPrimitives(boolean a, int b, long c, double d) {};
