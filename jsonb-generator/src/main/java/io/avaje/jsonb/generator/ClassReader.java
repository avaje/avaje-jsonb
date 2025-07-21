package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.jdkVersion;
import static io.avaje.jsonb.generator.APContext.previewEnabled;
import static io.avaje.jsonb.generator.ProcessingContext.useEnhancedSwitch;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

final class ClassReader implements BeanReader {

  private static final boolean useInstanceofPattern = jdkVersion() >= 17;
  private static final boolean nullSwitch = jdkVersion() >= 21 || jdkVersion() >= 17 && previewEnabled();

  private final TypeElement beanType;
  private final String shortName;
  private final String type;

  private final MethodReader constructor;
  private final List<FieldReader> allFields;
  private final List<MethodProperty> methodProperties;
  private final Set<String> importTypes = new TreeSet<>();
  private final NamingConvention namingConvention;
  private final boolean hasSubTypes;
  private final TypeReader typeReader;
  private final String typeProperty;
  private final boolean nonAccessibleField;
  private final boolean caseInsensitiveKeys;
  private final boolean readOnlyInterface;
  private FieldReader unmappedField;
  private boolean hasRaw;
  private final boolean isRecord;
  private final boolean usesTypeProperty;
  private final boolean useEnum;
  private final Map<String, Integer> frequencyMap = new HashMap<>();
  private final Map<String, Boolean> isCommonFieldMap = new HashMap<>();
  private final boolean optional;
  private final List<TypeSubTypeMeta> subTypes;
  private final boolean pkgPrivate;

  /** An Interface/abstract type with a single implementation */
  private ClassReader implementation;

  ClassReader(TypeElement beanType, String errorContext) {
    this(beanType, null, errorContext);
  }

  ClassReader(TypeElement beanType, TypeElement mixInElement, String errorContext) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    final NamingConventionReader ncReader = new NamingConventionReader(beanType);
    this.namingConvention = ncReader.get();
    this.typeProperty = ncReader.typeProperty();
    this.caseInsensitiveKeys = ncReader.isCaseInsensitiveKeys();
    this.typeReader = new TypeReader(errorContext, beanType, mixInElement, namingConvention, typePropertyKey());
    typeReader.process();
    this.nonAccessibleField = typeReader.nonAccessibleField();
    this.hasSubTypes = typeReader.hasSubTypes();
    this.allFields = typeReader.allFields();
    this.constructor = typeReader.constructor();
    this.optional = typeReader.hasOptional();
    this.isRecord = isRecord(beanType);
    this.subTypes = typeReader.subTypes();
    this.readOnlyInterface = typeReader.extendsThrowable() || allFields.isEmpty() && subTypes.isEmpty();
    this.methodProperties = typeReader.methodProperties();
    this.pkgPrivate = typeReader.isPkgPrivate();

    subTypes.stream().map(TypeSubTypeMeta::type).forEach(importTypes::add);

    final var userTypeField = allFields.stream().filter(f -> f.propertyName().equals(typePropertyKey())).findAny();
    this.usesTypeProperty = userTypeField.isPresent();
    this.useEnum =
      userTypeField
        .map(FieldReader::type)
        .map(GenericType::topType)
        .map(APContext::typeElement)
        .filter(e -> e.getKind() == ElementKind.ENUM)
        .isPresent();
  }

  @SuppressWarnings("unchecked")
  boolean isRecord(TypeElement beanType) {
    try {
      final var recordComponents = (List<? extends Element>) TypeElement.class.getMethod("getRecordComponents").invoke(beanType);
      return !recordComponents.isEmpty();
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      return false;
    }
  }

  /**
   * For an interface type set the single implementation to use for fromJson().
   */
  void setImplementationType(TypeElement implementationType) {
    this.implementation = new ClassReader(implementationType, "");
  }

  @Override
  public int genericTypeParamsCount() {
    return typeReader.genericTypeParamsCount();
  }

  @Override
  public String toString() {
    return beanType.toString();
  }

  @Override
  public String shortName() {
    return shortName;
  }

  @Override
  public TypeElement beanType() {
    return beanType;
  }

  List<FieldReader> allFields() {
    return allFields;
  }

  @Override
  public boolean supportsViewBuilder() {
    return !hasSubTypes;
  }

  @Override
  public boolean nonAccessibleField() {
    return nonAccessibleField;
  }

  @Override
  public boolean hasJsonAnnotation() {
    return JsonPrism.isPresent(beanType);
  }

  @Override
  public void read() {
    Optional.ofNullable(constructor)
      .ifPresent(c -> {
        var enclosing = (TypeElement) c.element().getEnclosingElement();
        importTypes.add(enclosing.getQualifiedName().toString());
      });

    importTypes.add(Util.shortName(type));
    for (final FieldReader field : allFields) {
      field.addImports(importTypes);
      if (field.isRaw()) {
        hasRaw = true;
      }
      if (field.isUnmapped()) {
        unmappedField = field;
      }
    }
    for (final MethodProperty methodProperty : methodProperties) {
      methodProperty.addImports(importTypes);
    }
  }

  private Set<String> importTypes() {
    if (genericTypeParamsCount() > 0) {
      importTypes.add(Constants.REFLECT_TYPE);
      importTypes.add(Constants.PARAMETERIZED_TYPE);
    }
    importTypes.add(Constants.IOEXCEPTION);
    if (!hasSubTypes) {
      importTypes.add(Constants.METHODHANDLE);
    }
    if (!ProcessingContext.isImported(beanType)) {
      importTypes.add(type);
    }
    for (final FieldReader allField : allFields) {
      allField.addImports(importTypes);
    }
    for (final MethodProperty methodProperty : methodProperties) {
      methodProperty.addImports(importTypes);
    }
    if (implementation != null) {
      implementation.addImported(importTypes);
    }
    if (supportsViewBuilder()) {
      importTypes.add("io.avaje.json.view.ViewBuilder");
      importTypes.add("io.avaje.json.view.ViewBuilderAware");
    }
    importTypes.add("io.avaje.json.JsonAdapter");
    importTypes.add("io.avaje.json.PropertyNames");
    importTypes.add("io.avaje.json.JsonReader");
    importTypes.add("io.avaje.json.JsonWriter");
    importTypes.add("io.avaje.jsonb.AdapterFactory");
    importTypes.add(Constants.JSONB);
    importTypes.add("io.avaje.jsonb.Types");
    importTypes.add("io.avaje.jsonb.spi.Generated");
    return importTypes;
  }

  private void addImported(Set<String> importTypes) {
    importTypes.add(type);
  }

  @Override
  public void writeImports(Append writer, String packageName) {
    for (final String importType : importTypes()) {
      if (Util.validImportType(importType, packageName)) {
        writer.append("import %s;", Util.sanitizeImports(importType)).eol();
      }
    }
    writer.eol();
  }

  @Override
  public void cascadeTypes(Set<String> types) {
    for (final FieldReader allField : allFields) {
      if (allField.include()) {
        allField.cascadeTypes(types);
      }
    }
  }

  @Override
  public void writeFields(Append writer) {
    writer.append("  // naming convention %s", namingConvention).eol();
    for (final FieldReader allField : allFields) {
      allField.writeDebug(writer);
    }
    writer.eol();
    if (hasRaw) {
      writer.append("  private final JsonAdapter<String> rawAdapter;").eol();
    }
    final Set<String> uniqueTypes = new HashSet<>();
    if (hasSubTypes) {
      writer.append("  private final JsonAdapter<String> stringJsonAdapter;").eol();
      uniqueTypes.add("String");
    }
    for (final FieldReader allField : allFields) {
      if (includeField(allField, uniqueTypes)) {
        allField.writeField(writer);
      }
    }
    for (final MethodProperty methodProperty : methodProperties) {
      if (uniqueTypes.add(methodProperty.adapterShortType())) {
        methodProperty.writeField(writer);
      }
    }
    writer.append("  private final PropertyNames names;").eol();
    writer.eol();
  }

  private static boolean includeField(FieldReader allField, Set<String> uniqueTypes) {
    return allField.include()
      && !allField.isRaw()
      && includeFieldUniqueType(allField, uniqueTypes);
  }

  private static boolean includeFieldUniqueType(FieldReader allField, Set<String> uniqueTypes) {
    return allField.hasCustomSerializer() && uniqueTypes.add(allField.adapterFieldName())
      || !allField.hasCustomSerializer() && uniqueTypes.add(allField.adapterShortType());
  }

  @Override
  public void writeConstructor(Append writer) {
    if (hasRaw) {
      writer.append("    this.rawAdapter = jsonb.rawAdapter();").eol();
    }

    final Set<String> uniqueTypes = new HashSet<>();
    if (hasSubTypes) {
      writer.append("    this.stringJsonAdapter = jsonb.adapter(String.class);").eol();
      uniqueTypes.add("String");
    }
    for (final FieldReader allField : allFields) {
      if (includeField(allField, uniqueTypes)) {
        if (hasSubTypes) {
          final var isCommonDiffType =
            allFields.stream()
              .filter(s -> s.fieldName().equals(allField.fieldName()))
              .anyMatch(f -> !allField.adapterShortType().equals(f.adapterShortType()));
          isCommonFieldMap.put(allField.fieldName(), isCommonDiffType);
        }
        allField.writeConstructor(writer);
      }
    }
    for (MethodProperty methodProperty : methodProperties) {
      if (uniqueTypes.add(methodProperty.adapterShortType())) {
        methodProperty.writeConstructor(writer);
      }
    }
    writer.append("    this.names = jsonb.properties(");
    if (hasSubTypes) {
      writer.append("\"").append(typeProperty).append("\", ");
    }
    writer.append(propertyNames());
    writer.append(");").eol();
  }

  private String propertyNames() {
    return readOnlyInterface ? propertyNamesReadOnly() : propertyNamesFields();
  }

  private String propertyNamesFields() {
    final StringBuilder builder = new StringBuilder();
    //set to prevent writing same key twice
    final var seen = new HashSet<String>();
    for (int i = 0, size = allFields.size(); i < size; i++) {
      final FieldReader fieldReader = allFields.get(i);
      if (!seen.add(fieldReader.propertyName())) {
        continue;
      }
      if (i > 0) {
        builder.append(", ");
      }
      if (usesTypeProperty && fieldReader.propertyName().equals(typePropertyKey())) {
        builder.append(" ");
        continue;
      }
      builder.append("\"").append(fieldReader.propertyName()).append("\"");
    }
    return builder.toString().replace(" , ", "");
  }

  private String propertyNamesReadOnly() {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < methodProperties.size(); i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append("\"").append(methodProperties.get(i).propertyName()).append("\"");
    }
    return builder.toString().replace(" , ", "");
  }

  @Override
  public void writeViewSupport(Append writer) {
    if (!hasSubTypes) {
      writeView(writer);
      writeViewBuild(writer);
    }
  }

  private void writeView(Append writer) {
    writer.eol();
    writer.append("  @SuppressWarnings(\"unchecked\")").eol();
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
    for (final FieldReader allField : allFields) {
      if (allField.includeToJson(null)) {
        allField.writeViewBuilder(writer, shortName);
      }
    }
    for (final MethodProperty methodProperty : methodProperties) {
      methodProperty.writeViewBuilder(writer, shortName);
    }
    writer.append("    builder.endObject();").eol();
    writer.append("  }").eol();
  }

  @Override
  public void writeToJson(Append writer) {
    try {
      final String varName = Util.initLower(shortName);
      writer.eol();
      writer.append("  @Override").eol();
      writer.append("  public void toJson(JsonWriter writer, %s %s) {", shortName, varName).eol();
      writer.append("    writer.beginObject(names);").eol();
      if (hasSubTypes) {
        writeToJsonForSubtypes(writer, varName);
      } else {
        writeToJsonForType(writer, varName, "    ", null);
      }
      writer.append("    writer.endObject();").eol();
      writer.append("  }").eol();
    } catch (RuntimeException e) {
      throw new IllegalStateException("Error writing toJson() on " + type, e);
    }
  }

  private void writeToJsonForSubtypes(Append writer, String varName) {
    if (hasSubTypes) {
      for (int i = 0; i < subTypes.size(); i++) {
        final TypeSubTypeMeta subTypeMeta = subTypes.get(i);
        final String subType = subTypeMeta.type();
        final String subTypeName = subTypeMeta.name();
        final String elseIf = i == 0 ? "if" : "else if";
        final String subTypeShort = Util.shortType(subTypeMeta.type());
        if (useInstanceofPattern) {
          writer.append("    %s (%s instanceof final %s sub) {", elseIf, varName, subTypeShort).eol();
        } else {
          writer.append("    %s (%s instanceof %s) {", elseIf, varName, subTypeShort).eol();
          writer.append("      %s sub = (%s) %s;", subTypeShort, subTypeShort, varName).eol();
        }
        writer.append("      writer.name(0);").eol();
        writer.append("      stringJsonAdapter.toJson(writer, \"%s\");", subTypeName).eol();
        writeToJsonForType(writer, "sub", "      ", subType);
        writer.append("    }").eol();
      }
    }
  }

  private void writeToJsonForType(Append writer, String varName, String prefix, String type) {
    for (final FieldReader allField : allFields) {
      if (usesTypeProperty && allField.propertyName().equals(typePropertyKey())) {
        continue;
      }
      if (allField.includeToJson(type)) {
        allField.writeToJson(writer, varName, prefix);
      }
    }
    for (final MethodProperty methodProperty : methodProperties) {
      methodProperty.writeToJson(writer, varName, prefix);
    }
  }

  @Override
  public void writeFromJson(Append writer) {
    final String varName = Util.initLower(shortName);
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) {", shortName, varName).eol();
    if (readOnlyInterface) {
      if (implementation == null) {
        writer.append("    throw new UnsupportedOperationException();").eol();
        writer.append("  }").eol();
      } else {
        implementation.writeFromJsonImplementation(writer, varName);
      }
      return;
    }
    writeFromJsonImplementation(writer, varName);
  }

  private void writeFromJsonImplementation(Append writer, String varName) {
    final boolean directLoad = constructor == null && !hasSubTypes && !optional;
    if (directLoad) {
      // default public constructor
      writer.append("    %s _$%s = new %s();", shortName, varName, shortName).eol();
    } else {
      writer.append("    // variables to read json values into, constructor params don't need _set$ flags").eol();
      for (final FieldReader allField : allFields) {
        if (allField.includeFromJson()) {
          if (isRecord) {
            allField.writeFromJsonVariablesRecord(writer);
          } else {
            allField.writeFromJsonVariables(writer);
          }
        }
      }
    }
    if (hasSubTypes && !usesTypeProperty) {
      writer.eol().append("    String type = null;").eol();
    }
    if (unmappedField != null) {
      if (unmappedJsonNodeType()) {
        writer.append("    var unmapped = io.avaje.json.node.JsonObject.create();").eol();
      } else {
        writer.append("    var unmapped = new java.util.LinkedHashMap<String, Object>();").eol();
      }
    }
    writeFromJsonSwitch(writer, directLoad, varName);
    writer.eol();
    if (hasSubTypes) {
      writeFromJsonWithSubTypes(writer);
      return;
    }
    if (!directLoad) {
      writeJsonBuildResult(writer, varName);
    } else if (unmappedField != null) {
      writer.append("   // unmappedField... ", varName).eol();
      unmappedField.writeFromJsonUnmapped(writer, varName);
    }
    if (directLoad) {
      writer.append("    return _$%s;", varName).eol();
    }
    writer.append("  }").eol();
  }

  private boolean unmappedJsonNodeType() {
    return unmappedField.type().topType().startsWith("io.avaje.json.node.");
  }

  private void writeJsonBuildResult(Append writer, String varName) {
    var buildFields = allFields.stream()
      .filter(FieldReader::includeFromJsonBuild)
      .collect(toList());

    boolean directReturn = buildFields.isEmpty();
    if (!directReturn) {
      writer.append("    // build and return %s", shortName).eol();
    } else {
      writer.append("    // direct return").eol();
      writer.append("    return ");
    }
    if (constructor == null) {
      if (directReturn) {
        writer.append("new %s(", shortName);
      } else {
        writer.append("    %s _$%s = new %s(", shortName, varName, shortName);
      }
    } else {
      if (!directReturn) {
        writer.append("    %s _$%s = ", shortName, varName);
      }
      writer.append(constructor.creationString());
      final List<MethodReader.MethodParam> params = constructor.getParams();
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }

        final var paramName = params.get(i).name();
        var name =
          allFields.stream()
            .filter(FieldReader::isConstructorParam)
            .filter(f -> f.propertyName().equals(paramName) || f.fieldName().equals(paramName))
            .map(FieldReader::fieldName)
            .findFirst()
            .orElse(paramName);

        // append increasing numbers to constructor params sharing names with other subtypes
        final var frequency = frequencyMap.compute(name, (k, v) -> v == null ? 0 : v + 1);
        // assuming name matches field here?
        writer.append(constructorParamName(name + (frequency == 0 ? "" : frequency.toString())));
      }
    }
    writer.append(");").eol();
    for (final FieldReader allField : buildFields) {
      frequencyMap.compute(allField.fieldName(), (k, v) -> v == null ? 0 : v + 1);
      allField.writeFromJsonSetter(writer, varName, "");
    }
    if (!directReturn) {
      writer.append("    return _$%s;", varName).eol();
    }
  }

  private void writeFromJsonWithSubTypes(Append writer) {
    final var typeVar = usesTypeProperty ? "_val$" + typePropertyKey() : "type";
    final var useSwitch = subTypes.size() >= 3;

    if (!useSwitch || !nullSwitch) {
      writer.append("    if (%s == null) {", typeVar).eol();
      writer.append("      throw new IllegalStateException(\"Missing Required %s property that determines deserialization type\");", typeProperty).eol();
      writer.append("    }").eol();
    }
    if (useSwitch) {
      if (useEnhancedSwitch()) {
        writer.append("    return switch (%s) {", typeVar).eol();
      } else {
        writer.append("    switch (%s) {", typeVar).eol();
      }
      if (nullSwitch) {
        writer.append("      case null -> ").append("throw new IllegalStateException(\"Missing Required %s property that determines deserialization type\");", typeProperty).eol();
      }
    }
    // another frequency map to append numbers to the subtype constructor params
    final Map<String, Integer> frequencyMap2 = new HashMap<>();
    final var req = new SubTypeRequest(typeVar, this, useSwitch, useEnum, frequencyMap2, isCommonFieldMap);

    for (final TypeSubTypeMeta subTypeMeta : subTypes) {
      final var varName = Util.initLower(Util.shortName(subTypeMeta.type()));
      subTypeMeta.writeFromJsonBuild(writer, varName, req);
    }
    if (useSwitch) {
      writer.append("      default").appendSwitchCase().eol().append("    ");
    }
    writer.append("    throw new IllegalStateException(\"Unknown value for %s property \" + %s);", typeProperty, typeVar).eol();
    if (useSwitch) {
      if (useEnhancedSwitch()) {
        writer.append("        }").eol();
        writer.append("    };").eol();
      } else {
        writer.append("    }").eol();
      }
    }
    writer.append("  }").eol();
  }

  String constructorParamName(String name) {
    if (unmappedField != null && unmappedField.fieldName().equals(name)) {
      return "unmapped";
    }
    return "_val$" + name;
  }

  private void writeFromJsonSwitch(Append writer, boolean defaultConstructor, String varName) {
    writer.eol();
    writer.append("    // read json").eol();
    writer.append("    reader.beginObject(names);").eol();
    writer.append("    while (reader.hasNextField()) {").eol();
    if (caseInsensitiveKeys) {
      writer.append("      final String origFieldName = reader.nextField();").eol();
      writer.append("      final String fieldName = origFieldName.toLowerCase();").eol();
    } else {
      writer.append("      final String fieldName = reader.nextField();").eol();
    }
    writer.append("      switch (fieldName) {").eol();
    if (hasSubTypes && !usesTypeProperty) {
      writer.append("        case \"%s\":", typePropertyKey()).eol();
      writer.append("          type = stringJsonAdapter.fromJson(reader);").eol();
      writer.append("          break;").eol();
    }
    // don't write same switch case twice
    final var seen = new HashSet<>();
    for (final FieldReader allField : allFields) {
      final var name = allField.propertyName();
      if (!seen.add(name)) {
        continue;
      }
      if (hasSubTypes) {
        final var isCommonFieldDiffType = isCommonFieldMap.get(name);
        if (isCommonFieldDiffType == null || !isCommonFieldDiffType) {
          allField.writeFromJsonSwitch(
            writer,
            defaultConstructor,
            varName,
            caseInsensitiveKeys,
            allFields.stream()
              .filter(x -> x.fieldName().equals(name))
              .flatMap(f -> f.aliases().stream())
              .collect(toList()));
        } else {
          // if subclass shares a field name with another subclass
          // write a special case statement
          writeSubTypeCase(
            name,
            writer,
            allFields.stream().filter(x -> x.fieldName().equals(name)).collect(toList()),
            defaultConstructor,
            varName);
        }

      } else
        allField.writeFromJsonSwitch(writer, defaultConstructor, varName, caseInsensitiveKeys, List.of());
    }
    writer.append("        default:").eol();
    final String unmappedFieldName = caseInsensitiveKeys ? "origFieldName" : "fieldName";
    if (unmappedField != null) {
      if (unmappedJsonNodeType()) {
        writer.append("          var value = jsonNodeAdapter.fromJson(reader);").eol();
        writer.append("          unmapped.add(%s, value);", unmappedFieldName).eol();
      } else {
        writer.append("          var value = objectJsonAdapter.fromJson(reader);").eol();
        writer.append("          unmapped.put(%s, value);", unmappedFieldName).eol();
      }
    } else {
      writer.append("          reader.unmappedField(%s);", unmappedFieldName).eol();
      writer.append("          reader.skipValue();").eol();
    }
    writer.append("      }").eol();
    writer.append("    }").eol();
    writer.append("    reader.endObject();").eol();
  }

  private void writeSubTypeCase(String name, Append writer, List<FieldReader> commonFields, boolean defaultConstructor, String varName) {
    writer.append("        case \"%s\":", name).eol();
    // get all possible aliases of this field from the subtypes
    for (final String alias : commonFields.stream().map(FieldReader::aliases).findFirst().orElseGet(List::of)) {
      final String propertyKey = caseInsensitiveKeys ? alias.toLowerCase() : alias;
      writer.append("        case \"%s\":", propertyKey).eol();
    }
    var elseIf = false;
    // write the case statements with subtypeCheck
    for (final FieldReader fieldReader : commonFields) {
      final var subtype = new ArrayList<>(fieldReader.subTypes().values()).get(0);
      final var setter = fieldReader.setter();
      final var adapterFieldName = fieldReader.adapterFieldName();
      final var fieldName = fieldReader.fieldNameWithNum();
      if (useEnum) {
        writer.append("          %sif (%s.equals(%s)) {", elseIf ? "else " : "", subtype.name(), "type").eol();
      } else {
        writer.append("          %sif (\"%s\".equals(%s)) {", elseIf ? "else " : "", subtype.name(), "type").eol();
      }
      elseIf = true;
      if (!fieldReader.isDeserialize()) {
        writer.append("            reader.skipValue();");
      } else if (defaultConstructor) {
        if (setter != null) {
          writer.append("            _$%s.%s(%s.fromJson(reader));", varName, setter.getName(), adapterFieldName);
        } else if (fieldReader.isPublicField()) {
          writer.append("            _$%s.%s = %s.fromJson(reader);", varName, fieldName, adapterFieldName);
        }
      } else {
        writer.append("            _val$%s = %s.fromJson(reader);", fieldName, adapterFieldName);
        if (!fieldReader.isConstructorParam()) {
          writer.eol().append("            _set$%s = true;", fieldName);
        }
      }
      writer.eol().append("          }").eol();
    }
    writer
        .append("          else {").eol()
        .append("            throw new IllegalStateException(\"Missing Required type3 property that determines deserialization type\");").eol()
        .append("          }").eol()
        .append("          break;").eol().eol();
  }

  private String typePropertyKey() {
    return caseInsensitiveKeys ? typeProperty.toLowerCase() : typeProperty;
  }

  @Override
  public boolean isPkgPrivate() {
    return pkgPrivate;
  }
}
