package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Set;

class FieldReader {

  private final Element element;
  private final boolean publicField;
  private final String rawType;
  private final GenericType genericType;
  private final String adapterFieldName;
  private final String adapterShortType;
  private final String fieldName;

  private MethodReader setter;
  private MethodReader getter;

  private boolean constructorParam;

  FieldReader(Element element) {
    this.element = element;
    this.fieldName = element.getSimpleName().toString();
    this.rawType = element.asType().toString();
    this.genericType = GenericType.parse(rawType);
    this.publicField = element.getModifiers().contains(Modifier.PUBLIC);

    String typeShortName = genericType.shortName();
    adapterShortType = "JsonAdapter<"+typeShortName+">";
    adapterFieldName = Character.toLowerCase(typeShortName.charAt(0)) + typeShortName.substring(1) + "JsonAdapter";
  }

  String getFieldName() {
    return fieldName;
  }

  void addImports(Set<String> importTypes) {
    genericType.addImports(importTypes);
  }

  void setterMethod(MethodReader setter) {
    this.setter = setter;
  }

  void getterMethod(MethodReader getter) {
    this.getter = getter;
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

  void writeField(Append writer) {
    writer.append("  private final JsonAdapter<");
    genericType.writeShort(writer);
    writer.append("> %s;", adapterFieldName).eol();
  }

  void writeConstructor(Append writer) {
    String asType = genericType.asTypeDeclaration();
    writer.append("    this.%s = jsonb.adapter(%s);", adapterFieldName, asType).eol();
  }

  void writeToJson(Append writer, String varName) {
    writer.append("    writer.name(\"%s\");", fieldName).eol();
    writer.append("    %s.toJson(writer, ", adapterFieldName);
    if (publicField) {
      writer.append("%s.%s);", varName, fieldName).eol();
    } else if (getter != null) {
      writer.append("%s.%s());", varName, getter.getName()).eol();
    } else {
      writer.append("FIXME: field is not public and has not getter ? ", varName).eol();
    }
  }
}
