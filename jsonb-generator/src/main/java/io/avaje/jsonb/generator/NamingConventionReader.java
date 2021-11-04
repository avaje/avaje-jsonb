package io.avaje.jsonb.generator;

import io.avaje.jsonb.Json;

import javax.lang.model.element.*;
import java.util.Map;

class NamingConventionReader {

  private static final String JSON_ANNOTATION = "io.avaje.jsonb.Json";
  private static final String NAMING_ATTRIBUTE = "naming()";

  private final Element element;

  NamingConventionReader(TypeElement element) {
    this.element = element;
  }

  NamingConvention get() {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (JSON_ANNOTATION.equals(mirror.getAnnotationType().toString())) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
          if (entry.getKey().toString().equals(NAMING_ATTRIBUTE)) {
            return NamingConvention.of(Json.Naming.valueOf(entry.getValue().toString()));
          }
        }
      }
    }
    return NamingConvention.of(Json.Naming.Match);
  }

}
