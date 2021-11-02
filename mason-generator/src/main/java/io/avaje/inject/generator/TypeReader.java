package io.avaje.inject.generator;

import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TypeReader {

  private final TypeElement beanType;
  private final Set<String> importTypes;
  private final TypeExtendsReader extendsReader;
  private Set<GenericType> genericTypes;
  private String typesRegister;

  TypeReader(TypeElement returnElement, ProcessingContext context) {
    this(returnElement, context, new LinkedHashSet<>());
  }

  private TypeReader(TypeElement beanType, ProcessingContext context, Set<String> importTypes) {
    this.beanType = beanType;
    this.importTypes = importTypes;
    this.extendsReader = new TypeExtendsReader(beanType, context);
  }

  String getTypesRegister() {
    return typesRegister;
  }

  void addImports(Set<String> importTypes) {
    importTypes.addAll(this.importTypes);
  }

  List<FieldReader> allFields() {
    return extendsReader.allFields();
  }

  MethodReader constructor() {
    return extendsReader.constructor();
  }

  Set<GenericType> getGenericTypes() {
    return genericTypes;
  }

  void process() {
    extendsReader.process();
    initRegistrationTypes();
  }

  private void initRegistrationTypes() {
    TypeAppender appender = new TypeAppender(importTypes);
    appender.add(extendsReader.getBaseType());
    this.genericTypes = appender.genericTypes();
    this.typesRegister = appender.asString();
  }

  void extraImports(Set<String> importTypes) {
    if (!genericTypes.isEmpty()) {
      //importTypes.add(Constants.TYPE);
      for (GenericType type : genericTypes) {
        type.addImports(importTypes);
      }
    }
  }
}
