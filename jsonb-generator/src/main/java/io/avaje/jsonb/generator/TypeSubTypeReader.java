package io.avaje.jsonb.generator;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

/** Read the @Json.SubType annotations. */
final class TypeSubTypeReader {

  private final TypeElement baseType;
  private final List<TypeSubTypeMeta> subTypes = new ArrayList<>();

  TypeSubTypeReader(TypeElement baseType) {
    this.baseType = baseType;
    read();
  }

  List<TypeSubTypeMeta> subTypes() {
    return subTypes;
  }

  boolean hasSubTypes() {
    return !subTypes.isEmpty();
  }

  void read() {

    for (final AnnotationMirror mirror : baseType.getAnnotationMirrors()) {

      final SubTypePrism subtypePrism = SubTypePrism.getInstance(mirror);
      final SubTypesPrism subtypesPrism = SubTypesPrism.getInstance(mirror);

      if (subtypePrism != null) {
        subTypes.add(new TypeSubTypeMeta(subtypePrism));
      } else if (subtypesPrism != null) {

        subtypesPrism.value().stream().map(TypeSubTypeMeta::new).forEach(subTypes::add);
      }
    }
  }
}
