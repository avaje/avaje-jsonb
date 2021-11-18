package org.example.jmh.model;

import io.avaje.jsonb.Json;

@Json
public record NarrowNamesRecord(
  String a,
  String b,
  String c,
  String d,
  String e
) {
}
