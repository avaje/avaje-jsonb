package org.example.customer.naming;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.Json.Naming;

@Json(naming = Naming.LowerUnderscore)
public record NLowUnder(String simple, String simplePlus, int myOneRed) {

}
