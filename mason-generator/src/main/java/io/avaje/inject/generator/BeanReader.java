package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

class BeanReader {

  private final ProcessingContext context;
  private final TypeElement beanType;
  private final String shortName;
  private final String type;

  private final MethodReader constructor;
  private final List<FieldReader> injectFields;
  private final List<MethodReader> injectMethods;
  private final List<MethodReader> factoryMethods;

  private final Set<String> importTypes = new TreeSet<>();
  private final TypeReader typeReader;

  private boolean writtenToFile;

  BeanReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.context = context;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.typeReader = new TypeReader(beanType, context);

    typeReader.process();
    this.injectMethods = typeReader.getInjectMethods();
    this.injectFields = typeReader.getInjectFields();
    this.factoryMethods = typeReader.getFactoryMethods();
    this.constructor = typeReader.getConstructor();
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  TypeElement getBeanType() {
    return beanType;
  }

  BeanReader read() {
    if (constructor != null) {
      constructor.addImports(importTypes);
    }
    for (FieldReader fields : injectFields) {
      fields.addImports(importTypes);
    }
    for (MethodReader methods : injectMethods) {
      methods.addImports(importTypes);
    }
    for (MethodReader factoryMethod : factoryMethods) {
      factoryMethod.addImports(importTypes);
    }
    return this;
  }

  List<MethodReader> getFactoryMethods() {
    return factoryMethods;
  }

  List<String> getInterfaces() {
    return typeReader.getInterfaces();
  }

  Set<GenericType> getGenericTypes() {
    return typeReader.getGenericTypes();
  }

  /**
   * Return the short name of the element.
   */
  private String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  private Set<String> importTypes() {
    //importTypes.add(Constants.GENERATED);
    if (Util.validImportType(type)) {
      importTypes.add(type);
    }
    typeReader.extraImports(importTypes);
    return importTypes;
  }

  void writeImports(Append writer) {
    for (String importType : importTypes()) {
      if (Util.validImportType(importType)) {
        writer.append("import %s;", importType).eol();
      }
    }
    writer.eol();
  }


}
