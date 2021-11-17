package org.example.jmh.model;

import io.avaje.jsonb.Json;

@Json
public record MyRecord(
  String prop1,
  String prop2,
  String prop3,
  String prop4,
  String prop5
) {
}
