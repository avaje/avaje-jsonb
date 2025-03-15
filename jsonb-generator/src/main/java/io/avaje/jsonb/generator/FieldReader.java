package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import java.util.*;

final class FieldReader {

  private final Map<String, TypeSubTypeMeta> subTypes = new LinkedHashMap<>();

  private final Element element;
  private final FieldProperty property;
  private final String propertyName;
  private final boolean serialize;
  private boolean deserialize;
  private final boolean unmapped;
  private final boolean raw;
  private final boolean hasCustomSerializer;

  private final List<String> aliases = new ArrayList<>();
  private boolean isSubTypeField;
  private final String num;
  private boolean isCreatorParam;
  private boolean useGetterAddAll;

  FieldReader(
      Element element,
      NamingConvention namingConvention,
      TypeSubTypeMeta subType,
      List<String> genericTypeParams,
      Integer frequency) {

    this(element, namingConvention, subType, genericTypeParams, frequency, false);
  }

  FieldReader(
      Element element,
      NamingConvention namingConvention,
      TypeSubTypeMeta subType,
      List<String> genericTypeParams,
      Integer frequency,
      boolean jsonCreatorPresent) {

    this.element = element;
    num = frequency == 0 ? "" : frequency.toString();
    addSubType(subType);
    var isMethod = element instanceof ExecutableElement;
    var isParam = element.getEnclosingElement() instanceof ExecutableElement;
    this.unmapped = UnmappedPrism.isPresent(element);
    this.raw = RawPrism.isPresent(element);

    final var fieldName = element.getSimpleName().toString();
    final var publicField = !isMethod && !isParam && Util.isPublic(element);
    final var type = isMethod ? ((ExecutableElement) element).getReturnType() : element.asType();

    final var customSerializer = SerializerPrism.getOptionalOn(element).map(SerializerPrism::value);
    this.hasCustomSerializer = customSerializer.isPresent();
    this.property =
      new FieldProperty(
        type,
        raw,
        unmapped,
        genericTypeParams,
        publicField,
        fieldName,
        customSerializer);
    this.propertyName = PropertyPrism.getOptionalOn(element)
      .map(PropertyPrism::value)
      .map(Util::escapeQuotes)
      .orElse(namingConvention.from(fieldName));

    final PropertyIgnoreReader ignoreReader = new PropertyIgnoreReader(element, propertyName);
    this.serialize = !isParam && ignoreReader.serialize();
    this.deserialize = isParam || !jsonCreatorPresent && !isMethod && ignoreReader.deserialize();

    initAliases(element);
  }

  void readParam(VariableElement element) {
    this.deserialize = true;
    this.isCreatorParam = true;
    property.setConstructorParam();
    initAliases(element);
  }

  private void initAliases(Element element) {
    var alias =
      AliasPrism.getOptionalOn(element)
        .map(a -> Util.escapeQuotes(a.value()))
        .orElse(Collections.emptyList());

    aliases.addAll(alias);
  }

  void position(int pos) {
    property.setPosition(pos);
  }

  String fieldName() {
    return property.fieldName();
  }

  String propertyName() {
    return propertyName;
  }

  boolean typeObjectBooleanWithIsPrefix() {
    return property.typeObjectBooleanWithIsPrefix();
  }

  boolean typeBooleanWithIsPrefix() {
    return property.typeBooleanWithIsPrefix();
  }

  boolean isRaw() {
    return raw;
  }

  boolean isUnmapped() {
    return unmapped;
  }

  boolean include() {
    return serialize || deserialize;
  }

  boolean includeFromJson() {
    return deserialize;
  }

  boolean includeFromJsonBuild() {
    return deserialize && !isCreatorParam && !property.isConstructorParam();
  }

  boolean includeToJson() {
    return serialize;
  }

  boolean includeToJson(String type) {
    return serialize && (type == null || subTypes.containsKey(type));
  }

  void addSubType(TypeSubTypeMeta currentSubType) {
    if (currentSubType != null) {
      subTypes.put(currentSubType.type(), currentSubType);
    }
  }

  void setSubTypeField() {
    this.isSubTypeField = true;
  }

  boolean isSubTypeField() {
    return isSubTypeField;
  }

  boolean includeForType(TypeSubTypeMeta subType) {
    return subTypes.containsKey(subType.type());
  }

  void addImports(Set<String> importTypes) {
    property.addImports(importTypes);
  }

  void cascadeTypes(Set<String> types) {
    property.cascadeTypes(types);
  }

  void setterMethod(MethodReader setter) {
    property.setSetterMethod(setter);
  }

  void getterMethod(MethodReader getter) {
    property.setGetterMethod(getter);
  }

  void setConstructorParam() {
    property.setConstructorParam();
  }

  boolean isPublicField() {
    return property.isPublicField();
  }

  boolean hasCustomSerializer() {
    return hasCustomSerializer;
  }

  void writeDebug(Append writer) {
    writer.append("  // %s [%s] name:%s", property.fieldName(), property.rawType(), propertyName);
    if (!serialize) {
      writer.append(" ignoreSerialize");
    }
    if (!deserialize) {
      writer.append(" ignoreDeserialize");
    } else if (property.isConstructorParam()) {
      writer.append(" constructor");
    } else if (property.setter() != null) {
      writer.append(" setter:%s ", property.setter());
    } else if (property.isPublicField()) {
      writer.append(" publicField");
    } else {
      writer.append(" ERROR?? no constructor, setter and not a public field?");
    }
    if (!subTypes.isEmpty()) {
      writer.append(" subTypes %s", subTypes.keySet());
    }
    writer.eol();
  }

  String adapterShortType() {
    return property.shortType();
  }

  void writeField(Append writer) {
    property.writeField(writer);
  }

  void writeConstructor(Append writer) {
    property.writeConstructor(writer);
  }

  void writeToJson(Append writer, String varName, String prefix) {
    property.writeToJson(writer, varName, prefix);
  }

  void writeFromJsonVariables(Append writer) {
    property.writeFromJsonVariables(writer, num);
  }

  void writeFromJsonVariablesRecord(Append writer) {
    property.writeFromJsonVariablesRecord(writer, num);
  }

  void writeFromJsonSwitch(Append writer, boolean defaultConstructor, String varName, boolean caseInsensitiveKeys, List<String> moreAlias) {
    if (unmapped) {
      return;
    }
    aliases.addAll(moreAlias);
    for (final String alias : aliases) {
      final String propertyKey = caseInsensitiveKeys ? alias.toLowerCase() : alias;
      writer.append("        case \"%s\":", propertyKey).eol();
    }
    final String propertyKey = caseInsensitiveKeys ? propertyName.toLowerCase() : propertyName;
    writer.append("        case \"%s\": ", propertyKey).eol();
    if (!deserialize) {
      writer.append("          reader.skipValue();");
    } else {
      property.writeFromJsonSwitch(writer, varName, defaultConstructor, useGetterAddAll);
    }
    writer.eol().append("          break;").eol().eol();
  }

  void writeFromJsonSetter(Append writer, String varName, String prefix) {
    if (isCreatorParam) return;
    property.writeFromJsonSetter(writer, varName, prefix, num);
  }

  void writeFromJsonUnmapped(Append writer, String varName) {
    property.writeFromJsonUnmapped(writer, varName);
  }

  void writeViewBuilder(Append writer, String shortName) {
    property.writeViewBuilder(writer, shortName, propertyName);
  }

  @Override
  public String toString() {
    return property.fieldName();
  }

  GenericType type() {
    return property.genericType();
  }

  boolean isConstructorParam() {
    return property.isConstructorParam();
  }

  String fieldNameWithNum() {
    return property.fieldName() + num;
  }

  String adapterFieldName() {
    return property.adapterFieldName();
  }

  MethodReader setter() {
    return property.setter();
  }

  boolean isDeserialize() {
    return deserialize;
  }

  Map<String, TypeSubTypeMeta> subTypes() {
    return subTypes;
  }

  List<String> aliases() {
    return aliases;
  }

  Element element() {
    return element;
  }

  void setUseGetterAddAll() {
    useGetterAddAll = true;
  }
}
