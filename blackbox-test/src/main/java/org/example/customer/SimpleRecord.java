package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public record SimpleRecord(int size, String name, Integer asObjInteger) {
}
