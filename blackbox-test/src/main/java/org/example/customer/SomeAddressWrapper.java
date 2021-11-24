package org.example.customer;

import io.avaje.jsonb.Json;

@Json
public record SomeAddressWrapper (Long id, Address address) {
}
