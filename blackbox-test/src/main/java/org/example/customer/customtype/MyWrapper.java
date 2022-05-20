package org.example.customer.customtype;

import io.avaje.jsonb.Json;

@Json
public record MyWrapper(int id, String base, MyCustomScalarType custom) {
}
