package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.ProcessingContext.jdkVersion;
import static io.avaje.jsonb.generator.ProcessingContext.previewEnabled;
import static io.avaje.jsonb.generator.ProcessingContext.useEnhancedSwitch;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

final class ClassReader implements BeanReader {

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
  private final boolean usesTypeProperty;
  private final boolean useEnum;
  private static final boolean useInstanceofPattern = jdkVersion() >= 17;
  private static final boolean nullSwitch = jdkVersion() >= 21 || (jdkVersion() >= 17 && previewEnabled());
  private final Map<String, Integer> frequencyMap = new HashMap<>();
  private final Map<String, Boolean> isCommonFieldMap = new HashMap<>();

  ClassReader(TypeElement beanType) {
    this(beanType, null);
  }

  ClassReader(TypeElement beanType, TypeElement mixInElement) {
    this.beanType = beanType;
    this.type = beanType.getQualifiedName().toString();
    this.shortName = shortName(beanType);
    final NamingConventionReader ncReader = new NamingConventionReader(beanType);
    this.namingConvention = ncReader.get();
    this.typeProperty = ncReader.typeProperty();
    this.caseInsensitiveKeys = ncReader.isCaseInsensitiveKeys();
    this.typeReader = new TypeReader(beanType, mixInElement, namingConvention, typePropertyKey());
    typeReader.process();
    this.nonAccessibleField = typeReader.nonAccessibleField();
    this.hasSubTypes = typeReader.hasSubTypes();
    this.allFields = typeReader.allFields();
    this.constructor = typeReader.constructor();
    this.isRecord = isRecord(beanType);
    typeReader.subTypes().stream().map(TypeSubTypeMeta::type).forEach(importTypes::add);

    final var userTypeField = allFields.stream().filter(f -> f.propertyName().equals(typePropertyKey())).findAny();

    this.usesTypeProperty = userTypeField.isPresent();
    this.useEnum =
      userTypeField
        .map(FieldReader::type)
        .map(GenericType::topType)
        .map(ProcessingContext::element)
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
  public TypeElement getBeanType() {
    return beanType;
  }

  List<FieldReader> allFields() {
    return allFields;
  }

  @Override
  public boolean hasSubtypes() {
    return hasSubTypes;
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
    for (final FieldReader field : allFields) {
      field.addImports(importTypes);
      if (field.isRaw()) {
        hasRaw = true;
      }
      if (field.isUnmapped()) {
        unmappedField = field;
      }
    }
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
    for (final FieldReader allField : allFields) {
      allField.addImports(importTypes);
    }
    return importTypes;
  }

  @Override
  public void writeImports(Append writer) {
    for (final String importType : importTypes()) {
      if (Util.validImportType(importType)) {
        writer.append("import %s;", importType).eol();
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
    for (final FieldReader allField : allFields) {
      if (allField.include() && !allField.isRaw() && uniqueTypes.add(allField.adapterShortType())) {
        allField.writeField(writer);
      }
    }
    writer.append("  private final PropertyNames names;").eol();
    writer.eol();
  }

  @Override
  public void writeConstructor(Append writer) {
    if (hasRaw) {
      writer.append("    this.rawAdapter = jsonb.rawAdapter();").eol();
    }
    final Set<String> uniqueTypes = new HashSet<>();
    for (final FieldReader allField : allFields) {
      if (allField.include() && !allField.isRaw() && uniqueTypes.add(allField.adapterShortType())) {
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
    writer.append("    this.names = jsonb.properties(");
    if (hasSubTypes) {
      writer.append("\"").append(typeProperty).append("\", ");
    }
    final StringBuilder builder = new StringBuilder();

    //set to prevent writing same key twice
    final var seen = new HashSet<String>();
    for (int i = 0, size = allFields.size(); i < size; i++) {
      final FieldReader fieldReader = allFields.get(i);
      if (!seen.add(fieldReader.fieldName())) {
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
    writer.append(builder.toString().replace(" , ", ""));
    writer.append(");").eol();
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
      for (final FieldReader allField : allFields) {
        if (allField.includeToJson(null)) {
          allField.writeViewBuilder(writer, shortName);
        }
      }
    }
    writer.append("    builder.endObject();").eol();
    writer.append("  }").eol();
  }

  @Override
  public void writeToJson(Append writer) {
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
  }

  private void writeToJsonForSubtypes(Append writer, String varName) {
    if (hasSubTypes) {
      final List<TypeSubTypeMeta> subTypes = typeReader.subTypes();
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
  }

  @Override
  public void writeFromJson(Append writer) {
    final String varName = Util.initLower(shortName);
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) {", shortName, varName).eol();
    final boolean directLoad = (constructor == null && !hasSubTypes);
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
    if (hasSubTypes && !usesTypeProperty) {
      writer.eol().append("    String type = null;").eol();
    }
    if (unmappedField != null) {
      writer.append("    Map<String, Object> unmapped = new LinkedHashMap<>();").eol();
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
    writer.append("    return _$%s;", varName).eol();
    writer.append("  }").eol();
  }

  private void writeJsonBuildResult(Append writer, String varName) {
    writer.append("    // build and return %s", shortName).eol();
    writer.append("    %s _$%s = new %s(", shortName, varName, shortName);
    final List<MethodReader.MethodParam> params = constructor.getParams();
    for (int i = 0, size = params.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      final var name = params.get(i).name();
      // append increasing numbers to constructor params sharing names with other subtypes
      final var frequency = frequencyMap.compute(name, (k, v) -> v == null ? 0 : v + 1);
      // assuming name matches field here?
      writer.append(constructorParamName(name + (frequency == 0 ? "" : frequency.toString())));
    }
    writer.append(");").eol();
    for (final FieldReader allField : allFields) {
      if (allField.includeFromJson()) {
        frequencyMap.compute(allField.fieldName(), (k, v) -> v == null ? 0 : v + 1);
        allField.writeFromJsonSetter(writer, varName, "");
      }
    }
  }

  private void writeFromJsonWithSubTypes(Append writer) {
    final var typeVar = usesTypeProperty ? "_val$" + typePropertyKey() : "type";
    final var useSwitch = typeReader.subTypes().size() >= 3;

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

    for (final TypeSubTypeMeta subTypeMeta : typeReader.subTypes()) {
      final var varName = Util.initLower(Util.shortName(subTypeMeta.type()));
         subTypeMeta.writeFromJsonBuild(writer, typeVar, varName, this, useSwitch, useEnum, frequencyMap2, isCommonFieldMap);
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
    if ((unmappedField != null) && unmappedField.fieldName().equals(name)) {
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
      final var name = allField.fieldName();
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
                  .flatMap(f -> f.getAliases().stream())
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
      writer.append("          Object value = objectJsonAdapter.fromJson(reader);").eol();
      writer.append("          unmapped.put(%s, value);", unmappedFieldName).eol();
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
    for (final String alias :
        commonFields.stream().map(FieldReader::getAliases).findFirst().orElseGet(List::of)) {
      final String propertyKey = caseInsensitiveKeys ? alias.toLowerCase() : alias;
      writer.append("        case \"%s\":", propertyKey).eol();
    }
    var elseIf = false;
    // write the case statements with subtypeCheck
    for (final FieldReader fieldReader : commonFields) {
      final var subtype = new ArrayList<>(fieldReader.getSubTypes().values()).get(0);
      final var setter = fieldReader.getSetter();
      final var adapterFieldName = fieldReader.getAdapterFieldName();
      final var fieldName = fieldReader.getFieldNameWithNum();
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
}
