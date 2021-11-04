package org.example.customer.naming;

import io.avaje.jsonb.Json;

import static io.avaje.jsonb.Json.Naming.UpperUnderscore;

@Json(naming = UpperUnderscore)
public record NUppUnder(String simple, String simplePlus, int myOneRed) {

}
