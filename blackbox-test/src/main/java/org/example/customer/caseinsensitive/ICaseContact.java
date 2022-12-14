package org.example.customer.caseinsensitive;

import io.avaje.jsonb.Json;

@Json(caseInsensitiveKeys = true)
public record ICaseContact(int id, String firstName, String lastName) {

}
