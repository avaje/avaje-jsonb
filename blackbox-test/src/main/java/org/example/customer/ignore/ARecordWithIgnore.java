package org.example.customer.ignore;

import io.avaje.jsonb.Json;

@Json
public record ARecordWithIgnore(String one, @Json.Ignore String two) {
}
