package org.example.customer.hash;

import io.avaje.jsonb.Json;

/**
 * Property names "glbvs" and "yacxa" have the same FNV-1a hash so the
 * generated adapter must fall back to a String switch.
 */
@Json
public record HashCollision(String glbvs, String yacxa, String other) {
}
