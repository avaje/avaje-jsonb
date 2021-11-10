package io.avaje.jsonb.generator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.Map;

class PropertyIgnoreReader {

  private static final String JSON_IGNORE = "io.avaje.jsonb.Json.Ignore";
  private boolean ignoreSerialize;
  private boolean ignoreDeserialize;

  PropertyIgnoreReader(Element element) {
    read(element);
  }

  boolean serialize() {
    return !ignoreSerialize;
  }

  boolean deserialize() {
    return !ignoreDeserialize;
  }

  /**
   * Read the Json.Property annotation using annotation mirrors.
   */
  void read(Element element) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (JSON_IGNORE.equals(mirror.getAnnotationType().toString())) {
        ignoreDeserialize = true;
        ignoreSerialize = true;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
          String key = entry.getKey().toString();
          String value = entry.getValue().toString();
          if (key.equals("deserialize()")) {
            ignoreDeserialize = "false".equals(value);
          } else if (key.equals("serialize()")) {
            ignoreSerialize = "false".equals(value);
          } else {
            throw new IllegalStateException("Unknown attribute on @Json.Ignore " + key);
          }
        }
      }
    }
  }

}
