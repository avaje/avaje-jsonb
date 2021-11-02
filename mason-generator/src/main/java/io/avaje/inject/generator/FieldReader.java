package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import java.util.Set;

class FieldReader {

  private final Element element;
  //private final String name;
  //private final UtilType type;
  private final boolean nullable;
  //private final String fieldType;
  private boolean requestParam;
  private String requestParamName;

  FieldReader(Element element) {
    this.element = element;
    //this.name = Util.getNamed(element);
    this.nullable = Util.isNullable(element);
    //this.type = Util.determineType(element.asType());
    //this.fieldType = Util.unwrapProvider(type.rawType());
  }

  String getFieldName() {
    return element.getSimpleName().toString();
  }

  void addImports(Set<String> importTypes) {
    //importTypes.add(fieldType);
  }

}
