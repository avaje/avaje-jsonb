package org.example.jmh.model;

import io.avaje.jsonb.Json;

import java.net.URI;

@Json
public record UriData(URI one, URI two) {
}
