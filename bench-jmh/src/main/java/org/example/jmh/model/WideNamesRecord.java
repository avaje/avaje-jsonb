package org.example.jmh.model;

import com.dslplatform.json.CompiledJson;
import io.avaje.jsonb.Json;

@CompiledJson
@Json
public record WideNamesRecord(
  String firstNameProperty1,
  String lastNameProperty2,
  String anotherSimilarProperty3,
  String moreOrLessProperty4,
  String lastButNotLeastProperty5
) {
}
