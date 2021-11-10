package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

class TypeSubTypeMeta {

  private String type;
  private String name;
  private TypeElement typeElement;
  private boolean defaultPublicConstructor;
  private List<MethodReader> publicConstructors = new ArrayList<>();

  void add(String key, String val) {
    if (key.equals("value()")) {
      type = Util.trimClassSuffix(val);
    } else if (key.equals("name()")) {
      name = Util.trimQuotes(val);
    }
  }

  void setElement(TypeElement element) {
    this.typeElement = element;
  }

  String type() {
    return type;
  }

  String name() {
    if (name != null) {
      return name;
    }
    return Util.shortName(type);
  }

  boolean matches(String otherType) {
    return type.equals(otherType);
  }

  void addConstructor(MethodReader methodReader) {
    if (methodReader.getParams().isEmpty()) {
      defaultPublicConstructor = true;
    }
    publicConstructors.add(methodReader);
  }
}
