package io.avaje.jsonb.generator;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

final class AliasReader {
  private AliasReader() {}

  private static final String JSON_ALIAS = "io.avaje.jsonb.Json.JsonAlias";

  /** Read the Json.Alias annotation using annotation mirrors. */
  static List<String> getAliases(Element element) {
    for (final AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (JSON_ALIAS.equals(mirror.getAnnotationType().toString())) {
        return mirror.getElementValues().values().stream()
            .flatMap(v -> ((List<?>) v.getValue()).stream())
            .map(Object::toString)
            .map(Util::trimQuotes)
            .collect(Collectors.toList());
      }
    }
    return null;
  }
}
