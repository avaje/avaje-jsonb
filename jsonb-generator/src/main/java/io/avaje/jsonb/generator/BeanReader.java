package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

  String adapterFullName() {
    return type + "JsonAdapter";
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
    Set<String> uniqueTypes = new HashSet<>();
    for (FieldReader allField : allFields) {
      if (uniqueTypes.add(allField.adapterShortType())) {
        allField.writeField(writer);
      }
    }
    writer.eol();
  }

  void writeConstructor(Append writer) {
    Set<String> uniqueTypes = new HashSet<>();
    for (FieldReader allField : allFields) {
      if (uniqueTypes.add(allField.adapterShortType())) {
        allField.writeConstructor(writer);
      }
    }
  }

  void writeToJson(Append writer) {
    String varName = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public void toJson(JsonWriter writer, %s %s) throws IOException {", shortName, varName).eol();
    writer.append("    writer.beginObject();").eol();
    for (FieldReader allField : allFields) {
      allField.writeToJson(writer, varName);
    }
    writer.append("    writer.endObject();").eol();
    writer.append("  }").eol();
  }

  void writeFromJson(Append writer) {
    String varName = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);

    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) throws IOException {", shortName, varName).eol();
    writer.append("    // variables to read json values into, constructor params don't need _set$ flags").eol();
    for (FieldReader allField : allFields) {
      allField.writeFromJsonVariables(writer);
    }
    writeFromJsonSwitch(writer);
    writer.eol();
    writer.append("    // build and return %s", shortName).eol();
    writer.append("    %s _$%s = new %s(", shortName, varName, shortName);
    if (constructor != null) {
      List<MethodReader.MethodParam> params = constructor.getParams();
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append("_val$").append(params.get(i).name()); // assuming name matches field here?
      }
    }
    writer.append(");").eol();
    for (FieldReader allField : allFields) {
      allField.writeFromJsonSetter(writer, varName);
    }
    writer.append("    return _$%s;", varName).eol();
    writer.append("  }").eol();
  }

  private void writeFromJsonSwitch(Append writer) {
    writer.eol();
    writer.append("    // read json").eol();
    writer.append("    reader.beginObject();").eol();
    writer.append("    while (reader.hasNextField()) {").eol();
    writer.append("      String fieldName = reader.nextField();").eol();
    writer.append("      switch (fieldName) {").eol();
    for (FieldReader allField : allFields) {
      allField.writeFromJsonSwitch(writer);
    }

    writer.append("        default: {").eol();
    writer.append("          // TODO: Support skip unknown field/value etc").eol();
    writer.append("          throw new IllegalStateException(\"fieldName \" + fieldName + \" not found \");").eol();
    writer.append("        }").eol();
    writer.append("      }").eol();
    writer.append("    }").eol();
    writer.append("    reader.endObject();").eol();
  }

}
