package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

final class BeanReader {

  private final TypeElement beanType;
  private final String shortName;
  private final String type;

  private final MethodReader constructor;
  private final List<FieldReader> allFields;
  private final Set<String> importTypes = new TreeSet<>();
  private final NamingConvention namingConvention;
  private final boolean hasSubTypes;
  private final TypeReader typeReader;
  private final String typeProperty;
  private final boolean nonAccessibleField;
  private final boolean caseInsensitiveKeys;
  private FieldReader unmappedField;
  private boolean hasRaw;
  private final boolean isRecord;

  BeanReader(TypeElement beanType) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    NamingConventionReader ncReader = new NamingConventionReader(beanType);
    this.namingConvention = ncReader.get();
    this.typeProperty = ncReader.typeProperty();
    this.caseInsensitiveKeys = ncReader.isCaseInsensitiveKeys();
    this.typeReader = new TypeReader(beanType, namingConvention);
    typeReader.process();
    this.nonAccessibleField = typeReader.nonAccessibleField();
    this.hasSubTypes = typeReader.hasSubTypes();
    this.allFields = typeReader.allFields();
    this.constructor = typeReader.constructor();
    this.isRecord = isRecord(beanType);
  }

  public BeanReader(TypeElement beanType, TypeElement mixInElement) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    final NamingConventionReader ncReader = new NamingConventionReader(beanType);
    this.namingConvention = ncReader.get();
    this.typeProperty = ncReader.typeProperty();
    this.caseInsensitiveKeys = ncReader.isCaseInsensitiveKeys();
    this.typeReader = new TypeReader(beanType, mixInElement, namingConvention);
    typeReader.process();
    this.nonAccessibleField = typeReader.nonAccessibleField();
    this.hasSubTypes = typeReader.hasSubTypes();
    this.allFields = typeReader.allFields();
    this.constructor = typeReader.constructor();
    this.isRecord = isRecord(beanType);
  }

  @SuppressWarnings("unchecked")
  boolean isRecord(TypeElement beanType) {
    try {
      final var recordComponents =
          (List<? extends Element>)
              TypeElement.class.getMethod("getRecordComponents").invoke(beanType);
      return !recordComponents.isEmpty();
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      return false;
    }
  }

  int genericTypeParamsCount() {
    return typeReader.genericTypeParamsCount();
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  String shortName() {
    return shortName;
  }

  TypeElement getBeanType() {
    return beanType;
  }

  List<FieldReader> allFields() {
    return allFields;
  }

  boolean hasSubtypes() {
    return hasSubTypes;
  }

  boolean nonAccessibleField() {
    return nonAccessibleField;
  }

  boolean hasJsonAnnotation() {
    return JsonPrism.isPresent(beanType);
  }

  void read() {
    for (FieldReader field : allFields) {
      field.addImports(importTypes);
      if (field.isRaw()) {
        hasRaw = true;
      }
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
    if (genericTypeParamsCount() > 0) {
      importTypes.add(Constants.REFLECT_TYPE);
      importTypes.add(Constants.PARAMETERIZED_TYPE);
    }
    importTypes.add(Constants.JSONB_WILD);
    importTypes.add(Constants.IOEXCEPTION);
    importTypes.add(Constants.JSONB_SPI);
    if (!hasSubTypes) {
      importTypes.add(Constants.METHODHANDLE);
    }
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

  void cascadeTypes(Set<String> types) {
    for (FieldReader allField : allFields) {
      if (allField.include()) {
        allField.cascadeTypes(types);
      }
    }
  }

  void writeFields(Append writer) {
    writer.append("  // naming convention %s", namingConvention).eol();
    for (FieldReader allField : allFields) {
      allField.writeDebug(writer);
    }
    writer.eol();
    if (hasRaw) {
      writer.append("  private final JsonAdapter<String> rawAdapter;").eol();
    }
    Set<String> uniqueTypes = new HashSet<>();
    for (FieldReader allField : allFields) {
      if (allField.include() && !allField.isRaw() && uniqueTypes.add(allField.adapterShortType())) {
        allField.writeField(writer);
      }
    }
    writer.append("  private final PropertyNames names;").eol();
    writer.eol();
  }

  void writeConstructor(Append writer) {
    if (hasRaw) {
      writer.append("    this.rawAdapter = jsonb.rawAdapter();").eol();
    }
    Set<String> uniqueTypes = new HashSet<>();
    for (FieldReader allField : allFields) {
      if (allField.include() && !allField.isRaw() && uniqueTypes.add(allField.adapterShortType())) {
        allField.writeConstructor(writer);
      }
    }
    writer.append("    this.names = jsonb.properties(");
    if (hasSubTypes) {
      writer.append("\"").append(typeProperty).append("\", ");
    }
    for (int i = 0, size = allFields.size(); i < size; i++) {
      FieldReader fieldReader = allFields.get(i);
      if (i > 0) {
        writer.append(", ");
      }
      writer.append("\"").append(fieldReader.propertyName()).append("\"");
    }
    writer.append(");").eol();
  }

  void writeViewSupport(Append writer) {
    if (!hasSubTypes) {
      writeView(writer);
      writeViewBuild(writer);
    }
  }

  private void writeView(Append writer) {
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public boolean isViewBuilderAware() {").eol();
    writer.append("    return true;").eol();
    writer.append("  }").eol().eol();
    writer.append("  @Override").eol();
    writer.append("  public ViewBuilderAware viewBuild() {").eol();
    writer.append("    return this;").eol();
    writer.append("  }").eol().eol();
  }

  private void writeViewBuild(Append writer) {
    writer.append("  @Override").eol();
    writer.append("  public void build(ViewBuilder builder, String name, MethodHandle handle) {").eol();
    writer.append("    builder.beginObject(name, handle);").eol();
    if (!hasSubTypes) {
      for (FieldReader allField : allFields) {
        if (allField.includeToJson(null)) {
          allField.writeViewBuilder(writer, shortName);
        }
      }
    }
    writer.append("    builder.endObject();").eol();
    writer.append("  }").eol();
  }

  void writeToJson(Append writer) {
    String varName = Util.initLower(shortName);
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public void toJson(JsonWriter writer, %s %s) {", shortName, varName).eol();
    writer.append("    writer.beginObject();").eol();
    writer.append("    writer.names(names);").eol();
    if (hasSubTypes) {
      writeToJsonForSubtypes(writer, varName);
    } else {
      writeToJsonForType(writer, varName, "    ", null);
    }
    writer.append("    writer.endObject();").eol();
    writer.append("  }").eol();
  }

  private void writeToJsonForSubtypes(Append writer, String varName) {
    if (hasSubTypes) {
      List<TypeSubTypeMeta> subTypes = typeReader.subTypes();
      for (int i = 0; i < subTypes.size(); i++) {
        TypeSubTypeMeta subTypeMeta = subTypes.get(i);
        String subType = subTypeMeta.type();
        String subTypeName = subTypeMeta.name();
        String elseIf = i == 0 ? "if" : "else if";
        writer.append("    %s (%s instanceof %s) {", elseIf, varName, subType).eol();
        writer.append("      %s sub = (%s)%s;", subType, subType, varName).eol();
        writer.append("      writer.name(0);").eol();
        writer.append("      stringJsonAdapter.toJson(writer, \"%s\");", subTypeName).eol();
        writeToJsonForType(writer, "sub", "      ", subType);
        writer.append("    }").eol();
      }
    }
  }

  private void writeToJsonForType(Append writer, String varName, String prefix, String type) {
    for (FieldReader allField : allFields) {
      if (allField.includeToJson(type)) {
        allField.writeToJson(writer, varName, prefix);
      }
    }
  }

  void writeFromJson(Append writer) {
    String varName = Util.initLower(shortName);
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) {", shortName, varName).eol();
    boolean directLoad = (constructor == null && !hasSubTypes);
    if (directLoad) {
      // default public constructor
      writer.append("    %s _$%s = new %s();", shortName, varName, shortName).eol();
    } else {
      writer.append("    // variables to read json values into, constructor params don't need _set$ flags").eol();
      for (final FieldReader allField : allFields) {
        if (isRecord) {
          allField.writeFromJsonVariablesRecord(writer);
        } else if (allField.includeFromJson()) {
          allField.writeFromJsonVariables(writer);
        }
      }
    }
    if (hasSubTypes) {
      writer.eol().append("    String type = null;").eol();
    }
    if (unmappedField != null) {
      writer.append("    Map<String, Object> unmapped = new LinkedHashMap<>();").eol();
    }
    writeFromJsonSwitch(writer, directLoad, varName);
    writer.eol();
    if (hasSubTypes) {
      writeFromJsonWithSubTypes(writer, varName);
      return;
    }
    if (!directLoad) {
      writeJsonBuildResult(writer, varName);
    } else if (unmappedField != null) {
      writer.append("   // unmappedField... ", varName).eol();
      unmappedField.writeFromJsonUnmapped(writer, varName);
    }
    writer.append("    return _$%s;", varName).eol();
    writer.append("  }").eol();
  }

  private void writeJsonBuildResult(Append writer, String varName) {
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
        allField.writeFromJsonSetter(writer, varName, "");
      }
    }
  }

  private void writeFromJsonWithSubTypes(Append writer, String varName) {
    writer.append("    if (type == null) {").eol();
    writer.append("      throw new IllegalStateException(\"Missing %s property which is required?\");", typeProperty).eol();
    writer.append("    }").eol();
    for (TypeSubTypeMeta subTypeMeta : typeReader.subTypes()) {
      subTypeMeta.writeFromJsonBuild(writer, varName, this);
    }
    writer.append("    throw new IllegalStateException(\"Unknown value for %s property \" + type);", typeProperty).eol();
    writer.append("  }").eol();
  }

  String constructorParamName(String name) {
    if ((unmappedField != null) && unmappedField.fieldName().equals(name)) {
      return "unmapped";
    }
    return "_val$" + name;
  }

  private void writeFromJsonSwitch(Append writer, boolean defaultConstructor, String varName) {
    writer.eol();
    writer.append("    // read json").eol();
    writer.append("    reader.beginObject();").eol();
    writer.append("    reader.names(names);").eol();
    writer.append("    while (reader.hasNextField()) {").eol();
    if (caseInsensitiveKeys) {
      writer.append("      final String origFieldName = reader.nextField();").eol();
      writer.append("      final String fieldName = origFieldName.toLowerCase();").eol();
    } else {
      writer.append("      final String fieldName = reader.nextField();").eol();
    }
    writer.append("      switch (fieldName) {").eol();
    if (hasSubTypes) {
      writer.append("        case \"%s\": {", typePropertyKey()).eol();
      writer.append("          type = stringJsonAdapter.fromJson(reader); break;").eol();
      writer.append("        }").eol();
    }
    for (FieldReader allField : allFields) {
      allField.writeFromJsonSwitch(writer, defaultConstructor, varName, caseInsensitiveKeys);
    }
    writer.append("        default: {").eol();
    String unmappedFieldName = caseInsensitiveKeys ? "origFieldName" : "fieldName";
    if (unmappedField != null) {
      writer.append("          Object value = objectJsonAdapter.fromJson(reader);").eol();
      writer.append("          unmapped.put(%s, value);", unmappedFieldName).eol();
    } else {
      writer.append("          reader.unmappedField(%s);", unmappedFieldName).eol();
      writer.append("          reader.skipValue();").eol();
    }
    writer.append("        }").eol();
    writer.append("      }").eol();
    writer.append("    }").eol();
    writer.append("    reader.endObject();").eol();
  }

  private String typePropertyKey() {
    return caseInsensitiveKeys ? typeProperty.toLowerCase() : typeProperty;
  }

}
