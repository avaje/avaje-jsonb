package org.example.customer;

import io.avaje.jsonb.Json;

import java.util.UUID;

@Json
public record Contact(UUID id, String firstName, String lastName) {

}
