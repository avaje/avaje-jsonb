package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public record SomeBinary(int id, byte[] content) {

}
