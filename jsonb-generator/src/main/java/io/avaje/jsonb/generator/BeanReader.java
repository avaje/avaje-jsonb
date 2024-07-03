package io.avaje.jsonb.generator;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

interface BeanReader {

  void read();

  void writeImports(Append writer, String packageName);

  void writeFields(Append writer);

  void writeConstructor(Append writer);

  default void writeViewSupport(Append writer) {}

  void writeToJson(Append writer);

  void writeFromJson(Append writer);

  boolean supportsViewBuilder();

  String shortName();

  /** Return the short name of the element. */
  default String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  default int genericTypeParamsCount() {
    return 0;
  }

  TypeElement beanType();

  void cascadeTypes(Set<String> extraTypes);

  default boolean nonAccessibleField() {
    return false;
  }

  default boolean hasJsonAnnotation() {
    return false;
  }
}
