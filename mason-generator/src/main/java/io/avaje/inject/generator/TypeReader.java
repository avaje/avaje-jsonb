package io.avaje.inject.generator;

import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TypeReader {

  private final boolean forBean;
  private final TypeElement beanType;
  private final Set<String> importTypes;
  private final TypeExtendsReader extendsReader;
  private Set<GenericType> genericTypes;
  private String typesRegister;

  TypeReader(TypeElement returnElement, ProcessingContext context) {
    this(false, returnElement, context, new LinkedHashSet<>());
  }

  private TypeReader(boolean forBean, TypeElement beanType, ProcessingContext context, Set<String> importTypes) {
    this.forBean = forBean;
    this.beanType = beanType;
    this.importTypes = importTypes;
    this.extendsReader = new TypeExtendsReader(beanType, context);
  }

  String getTypesRegister() {
    return typesRegister;
  }

  List<String> getInterfaces() {
    return extendsReader.getInterfaceTypes();
  }

  void addImports(Set<String> importTypes) {
    importTypes.addAll(this.importTypes);
  }

  List<FieldReader> getInjectFields() {
    return extendsReader.getInjectFields();
  }

  List<MethodReader> getInjectMethods() {
    return extendsReader.getInjectMethods();
  }

  List<MethodReader> getFactoryMethods() {
    return extendsReader.getFactoryMethods();
  }

  MethodReader getConstructor() {
    return extendsReader.getConstructor();
  }

  Set<GenericType> getGenericTypes() {
    return genericTypes;
  }

  void process() {
    extendsReader.process(forBean);
    initRegistrationTypes();
  }

  private void initRegistrationTypes() {
    TypeAppender appender = new TypeAppender(importTypes);
    appender.add(extendsReader.getBaseType());
    appender.add(extendsReader.getExtendsTypes());
    appender.add(extendsReader.getInterfaceTypes());
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
