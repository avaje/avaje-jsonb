package org.example.jmh.model;

import io.avaje.jsonb.Json;

import java.net.URL;

@Json
public record UrlData(URL one, URL two) {
}
