package org.example.customer;

import io.avaje.jsonb.Json;
import java.util.List;

@Json
public record SomeAddressWrapper (Long id, Address address, List<String> tags) {
}
