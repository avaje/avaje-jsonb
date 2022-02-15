package org.example.jmh.model;

import com.dslplatform.json.CompiledJson;
import io.avaje.jsonb.Json;

@CompiledJson
@Json
public record NarrowNamesRecord(
  String a,
  String b,
  String c,
  String d,
  String e
) {
}
