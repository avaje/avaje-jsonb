package org.example.jmh.model;

import io.avaje.jsonb.Json;

@Json
public record NestAddress(
  String street1,
  String street2,
  String suburb,
  String city
) {
}
