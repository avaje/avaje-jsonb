package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

class BeanReader {

  private final ProcessingContext context;
  private final TypeElement beanType;
  private final String shortName;
  private final String type;

  private final MethodReader constructor;
  private final List<FieldReader> allFields;

  private final Set<String> importTypes = new TreeSet<>();
  private final TypeReader typeReader;

  BeanReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.context = context;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.typeReader = new TypeReader(beanType, context);

    typeReader.process();
    this.allFields = typeReader.allFields();
    this.constructor = typeReader.constructor();
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  TypeElement getBeanType() {
    return beanType;
  }

  BeanReader read() {
    for (FieldReader fields : allFields) {
      fields.addImports(importTypes);
    }
    return this;
  }

  /**
   * Return the short name of the element.
   */
  private String shortName(Element element) {
    return element.getSimpleName().toString();
  }

  private Set<String> importTypes() {
    importTypes.add(Constants.JSONB_WILD);
    importTypes.add(Constants.IOEXCEPTION);

    if (Util.validImportType(type)) {
      importTypes.add(type);
    }
    typeReader.extraImports(importTypes);
    for (FieldReader allField : allFields) {
      allField.addImports(importTypes);
    }
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


  void writeFields(Append writer) {
    for (FieldReader allField : allFields) {
      allField.writeDebug(writer);
    }
    writer.eol();
    for (FieldReader allField : allFields) {
      allField.writeField(writer);
    }
    writer.eol();
  }

  void writeConstructor(Append writer) {
    for (FieldReader allField : allFields) {
      allField.writeConstructor(writer);
    }
  }

  void writeToJson(Append writer){
    String varName = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);
    writer.append("  public void toJson(JsonWriter writer, %s %s) throws IOException {", shortName, varName).eol();
    writer.append("    writer.beginObject();").eol();
    for (FieldReader allField : allFields) {
      allField.writeToJson(writer, varName);
    }
    writer.append("    writer.endObject();").eol();
    writer.append("  }").eol();
  }
}
