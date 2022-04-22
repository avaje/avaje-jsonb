package org.example.customer.stream;

import io.avaje.jsonb.Json;

import java.util.List;

@Json
public record MyNested(int nest, String desc, List<MyBasic> innerStream) {
}
