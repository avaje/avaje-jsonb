package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class FieldReader {

  private final Map<String, TypeSubTypeMeta> subTypes = new LinkedHashMap<>();

  private final FieldProperty property;
  private final String propertyName;
  private final boolean serialize;
  private final boolean deserialize;
  private final boolean unmapped;
  private final boolean raw;

  private final List<String> aliases;
  private boolean isSubTypeField;
  private final String num;

  FieldReader(Element element, NamingConvention namingConvention, TypeSubTypeMeta subType, List<String> genericTypeParams, Integer frequency) {
    num = frequency == 0 ? "" : frequency.toString();
    addSubType(subType);
    final PropertyIgnoreReader ignoreReader = new PropertyIgnoreReader(element);
    this.unmapped = ignoreReader.unmapped();
    this.raw = ignoreReader.raw();
    this.serialize = ignoreReader.serialize();
    this.deserialize = ignoreReader.deserialize();

    final var fieldName = element.getSimpleName().toString();
    final var publicField = element.getModifiers().contains(Modifier.PUBLIC);
    this.property = new FieldProperty(element.asType(), raw, unmapped, genericTypeParams, publicField, fieldName);
    this.propertyName =
      PropertyPrism.getOptionalOn(element)
        .map(PropertyPrism::value)
        .map(Util::escapeQuotes)
        .orElse(namingConvention.from(fieldName));

    this.aliases =
      JsonAliasPrism.getOptionalOn(element)
        .map(JsonAliasPrism::value)
        .stream()
        .flatMap(List::stream)
        .map(Util::escapeQuotes)
        .collect(Collectors.toList());
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
    property.setSetter(setter);
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
      property.writeFromJsonSwitch(writer, varName, defaultConstructor);
    }
    writer.eol().append("          break;").eol().eol();
  }

  void writeFromJsonSetter(Append writer, String varName, String prefix) {
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

  public GenericType type() {
    return property.genericType();
  }

  public boolean isConstructorParam() {
    return property.isConstructorParam();
  }

  public String getFieldNameWithNum() {
    return property.fieldName() + num;
  }

  public String getAdapterFieldName() {
    return property.adapterFieldName();
  }

  public MethodReader getSetter() {
    return property.setter();
  }

  public void setSetter(MethodReader setter) {
    property.setSetter(setter);
  }

  public boolean isDeserialize() {
    return deserialize;
  }

  public Map<String, TypeSubTypeMeta> getSubTypes() {
    return subTypes;
  }

  public List<String> getAliases() {
    return aliases;
  }
}
