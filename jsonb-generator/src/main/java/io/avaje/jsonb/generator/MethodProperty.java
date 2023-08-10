package io.avaje.jsonb.generator;

import java.util.Set;

final class MethodProperty {

  private final FieldProperty property;
  private final String propertyName;

  MethodProperty(String propertyName, FieldProperty property) {
    this.property = property;
    this.propertyName = propertyName;
  }

  void addImports(Set<String> importTypes) {
    property.addImports(importTypes);
  }

  void writeToJson(Append writer, String varName, String prefix) {
    property.writeToJson(writer, varName, prefix);
  }

  void writeField(Append writer) {
    property.writeField(writer);
  }

  String adapterShortType() {
    return property.shortType();
  }

  void writeConstructor(Append writer) {
    property.writeConstructor(writer);
  }

  String propertyName() {
    return propertyName;
  }

  void writeViewBuilder(Append writer, String shortName) {
    property.writeViewBuilder(writer, shortName, propertyName());
  }
}
