package io.avaje.jsonb.generator;

import javax.lang.model.element.*;
import java.util.*;

/**
 * Read the @Json.SubType annotations.
 */
final class TypeSubTypeReader {

  private static final String JSON_SUBTYPE = "io.avaje.jsonb.Json.SubType";
  private static final String JSON_SUBTYPES = "io.avaje.jsonb.Json.SubTypes";

  private final TypeElement baseType;
  private final ProcessingContext context;
  private final List<TypeSubTypeMeta> subTypes = new ArrayList<>();

  TypeSubTypeReader(TypeElement baseType, ProcessingContext context) {
    this.baseType = baseType;
    this.context = context;
    read();
  }

  List<TypeSubTypeMeta> subTypes() {
    return subTypes;
  }

  boolean hasSubTypes() {
    return !subTypes.isEmpty();
  }

  void read() {
    for (AnnotationMirror mirror : baseType.getAnnotationMirrors()) {
      String annType = mirror.getAnnotationType().toString();
      if (JSON_SUBTYPE.equals(annType)) {
        readSubType(mirror);
      } else if (JSON_SUBTYPES.equals(annType)) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
          for (Object importType : (List<?>) entry.getValue().getValue()) {
            readSubType((AnnotationMirror)importType);
          }
        }
      }
    }
  }

  private void readSubType(AnnotationMirror mirror) {
    TypeSubTypeMeta meta = new TypeSubTypeMeta();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
      String key = entry.getKey().toString();
      String val = entry.getValue().toString();
      meta.add(key, val);
    }
    //context.logError("subtype attr "+attributes);
    subTypes.add(meta);
  }

}
