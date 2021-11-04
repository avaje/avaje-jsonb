package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;
import java.util.List;

class TypeReader {

  private final TypeExtendsReader extendsReader;

  TypeReader(TypeElement beanType, ProcessingContext context) {
    this.extendsReader = new TypeExtendsReader(beanType, context);
  }

  List<FieldReader> allFields() {
    return extendsReader.allFields();
  }

  MethodReader constructor() {
    return extendsReader.constructor();
  }

  void process() {
    extendsReader.process();
  }

}
