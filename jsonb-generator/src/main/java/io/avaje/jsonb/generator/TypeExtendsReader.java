package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * Read the inheritance types for a given bean type.
 */
class TypeExtendsReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private final TypeElement baseType;
  private final ProcessingContext context;
  private final TypeExtendsInjection extendsInjection;

  TypeExtendsReader(TypeElement baseType, ProcessingContext context, NamingConvention namingConvention) {
    this.baseType = baseType;
    this.context = context;
    this.extendsInjection = new TypeExtendsInjection(baseType, context, namingConvention);
  }

  List<FieldReader> allFields() {
    return extendsInjection.allFields();
  }

  MethodReader constructor() {
    return extendsInjection.constructor();
  }

  void process() {
    String base = baseType.getQualifiedName().toString();
    if (!GenericType.isGeneric(base)) {
      extendsInjection.read(baseType);
    }
    TypeElement superElement = superOf(baseType);
    if (superElement != null) {
      addSuperType(superElement);
    }
    extendsInjection.processCompleted();
  }

  private void addSuperType(TypeElement element) {
    String type = element.getQualifiedName().toString();
    if (!type.equals(JAVA_LANG_OBJECT)) {
      if (!GenericType.isGeneric(type)) {
        extendsInjection.read(element);
        addSuperType(superOf(element));
      }
    }
  }

  private TypeElement superOf(TypeElement element) {
    return (TypeElement) context.asElement(element.getSuperclass());
  }

}
