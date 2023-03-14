package io.avaje.jsonb.generator;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public interface BeanReader {

  void read();

  void writeImports(Append writer);

  void writeFields(Append writer);

  void writeConstructor(Append writer);

  default void writeViewSupport(Append writer) {}

  void writeToJson(Append writer);

  void writeFromJson(Append writer);

 boolean hasSubtypes();

  String shortName();

  /** Return the short name of the element. */
  default String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  default int genericTypeParamsCount() {
    return 0;
  }

  TypeElement getBeanType();

  void cascadeTypes(Set<String> extraTypes);

  default boolean nonAccessibleField() {
    return false;
  }

  default boolean hasJsonAnnotation() {
    return false;
  }
}
