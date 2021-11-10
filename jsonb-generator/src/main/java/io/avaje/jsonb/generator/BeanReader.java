package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class BeanReader {

  private final TypeElement beanType;
  private final String shortName;
  private final String type;

  private final MethodReader constructor;
  private final List<FieldReader> allFields;
  private final Set<String> importTypes = new TreeSet<>();
  private final NamingConvention namingConvention;
  private FieldReader unmappedField;

  BeanReader(TypeElement beanType, ProcessingContext context) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    this.namingConvention = new NamingConventionReader(beanType).get();

    TypeReader typeReader = new TypeReader(beanType, context, namingConvention);
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

  void read() {
    for (FieldReader field : allFields) {
      field.addImports(importTypes);
      if (field.isUnmapped()) {
        unmappedField = field;
      }
    }
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
    writer.append("  // naming convention %s", namingConvention).eol();
    for (FieldReader allField : allFields) {
      allField.writeDebug(writer);
    }
    writer.eol();
    Set<String> uniqueTypes = new HashSet<>();
    for (FieldReader allField : allFields) {
      if (allField.include()) {
        if (uniqueTypes.add(allField.adapterShortType())) {
          allField.writeField(writer);
        }
      }
    }
    writer.eol();
  }

  void writeConstructor(Append writer) {
    Set<String> uniqueTypes = new HashSet<>();
    for (FieldReader allField : allFields) {
      if (allField.include()) {
        if (uniqueTypes.add(allField.adapterShortType())) {
          allField.writeConstructor(writer);
        }
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
      if (allField.includeToJson()) {
        allField.writeToJson(writer, varName);
      }
    }
    writer.append("    writer.endObject();").eol();
    writer.append("  }").eol();
  }

  void writeFromJson(Append writer) {
    String varName = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);

    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) throws IOException {", shortName, varName).eol();
    boolean defaultConstructor = (constructor == null);
    if (defaultConstructor) {
      // default public constructor
      writer.append("    %s _$%s = new %s();", shortName, varName, shortName).eol();
    } else {
      writer.append("    // variables to read json values into, constructor params don't need _set$ flags").eol();
      for (FieldReader allField : allFields) {
        if (allField.includeFromJson()) {
          allField.writeFromJsonVariables(writer);
        }
      }
    }
    if (unmappedField != null) {
      writer.append("    Map<String, Object> unmapped = new LinkedHashMap<>();").eol();
    }
    writeFromJsonSwitch(writer, defaultConstructor, varName);
    writer.eol();
    if (!defaultConstructor) {
      writer.append("    // build and return %s", shortName).eol();
      writer.append("    %s _$%s = new %s(", shortName, varName, shortName);
      List<MethodReader.MethodParam> params = constructor.getParams();
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }
        writer.append(constructorParamName(params.get(i).name())); // assuming name matches field here?
      }
      writer.append(");").eol();
      for (FieldReader allField : allFields) {
        if (allField.includeFromJson()) {
          allField.writeFromJsonSetter(writer, varName);
        }
      }
    } else {
      if (unmappedField != null) {
        writer.append("   // unmappedField... ", varName).eol();
        unmappedField.writeFromJsonUnmapped(writer, varName);
      }
    }
    writer.append("    return _$%s;", varName).eol();
    writer.append("  }").eol();
  }

  private String constructorParamName(String name) {
    if (unmappedField != null) {
      if (unmappedField.getFieldName().equals(name)) {
        return "unmapped";
      }
    }
    return "_val$" + name;
  }

  private void writeFromJsonSwitch(Append writer, boolean defaultConstructor, String varName) {
    writer.eol();
    writer.append("    // read json").eol();
    writer.append("    reader.beginObject();").eol();
    writer.append("    while (reader.hasNextField()) {").eol();
    writer.append("      String fieldName = reader.nextField();").eol();
    writer.append("      switch (fieldName) {").eol();
    for (FieldReader allField : allFields) {
      allField.writeFromJsonSwitch(writer, defaultConstructor, varName);
    }
    writer.append("        default: {").eol();
    if (unmappedField != null) {
      writer.append("          Object value = objectJsonAdapter.fromJson(reader);").eol();
      writer.append("          unmapped.put(fieldName, value);").eol();
    } else {
      writer.append("          reader.unmappedField(fieldName);").eol();
      writer.append("          reader.skipValue();").eol();
    }
    writer.append("        }").eol();
    writer.append("      }").eol();
    writer.append("    }").eol();
    writer.append("    reader.endObject();").eol();
  }

}
