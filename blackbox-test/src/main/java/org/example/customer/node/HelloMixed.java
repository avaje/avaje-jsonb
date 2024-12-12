package org.example.customer.node;

import io.avaje.json.node.JsonNode;
import io.avaje.jsonb.Json;

@Json
public record HelloMixed(String name, JsonNode other) {
}
