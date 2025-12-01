package org.example.customer.nesting;

import io.avaje.jsonb.Json;

import java.util.List;

@Json
public record MyNestedList(List<List<Long>> nestedInts) {
}
