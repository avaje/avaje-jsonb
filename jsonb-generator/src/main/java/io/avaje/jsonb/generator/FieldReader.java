package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Set;

class FieldReader {

  private final Element element;
  private final boolean publicField;
  private final String rawType;
  private MethodReader setter;
  private boolean constructorParam;

  FieldReader(Element element) {
    this.element = element;
    this.rawType = element.asType().toString();
    this.publicField = element.getModifiers().contains(Modifier.PUBLIC);
  }

  String getFieldName() {
    return element.getSimpleName().toString();
  }

  void addImports(Set<String> importTypes) {
    //importTypes.add(fieldType);
  }

  void setterMethod(MethodReader setter) {
    this.setter = setter;
  }

  void constructorParam() {
    constructorParam = true;
  }

  boolean isPublic() {
    return publicField;
  }

  void writeDebug(Append writer) {
    writer.append("  // %s [%s] setter:%s constructor:%s public:%s", getFieldName(), rawType, setter, constructorParam, publicField).eol();
  }
}
