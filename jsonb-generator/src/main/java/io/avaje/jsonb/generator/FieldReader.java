package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

final class FieldReader {

  private final Map<String, TypeSubTypeMeta> subTypes = new LinkedHashMap<>();
  private final List<String> genericTypeParams;
  private final boolean publicField;
  private final String rawType;
  private final GenericType genericType;
  private final String adapterFieldName;
  private final String adapterShortType;
  private final String fieldName;
  private final boolean primitive;
  private final String defaultValue;
  private final String propertyName;
  private final boolean serialize;
  private final boolean deserialize;
  private final boolean unmapped;
  private final boolean raw;

  private MethodReader setter;
  private MethodReader getter;
  private int position;
  private boolean constructorParam;
  private boolean genericTypeParameter;
  private int genericTypeParamPosition;
  private final List<String> aliases;

  FieldReader(Element element, NamingConvention namingConvention, TypeSubTypeMeta subType, List<String> genericTypeParams) {
    addSubType(subType);
    this.genericTypeParams = genericTypeParams;
    this.fieldName = element.getSimpleName().toString();

    this.publicField = element.getModifiers().contains(Modifier.PUBLIC);
    this.rawType = trimAnnotations(element.asType().toString());

    final PropertyIgnoreReader ignoreReader = new PropertyIgnoreReader(element);
    this.unmapped = ignoreReader.unmapped();
    this.raw = ignoreReader.raw();
    this.serialize = ignoreReader.serialize();
    this.deserialize = ignoreReader.deserialize();

    this.propertyName =
      Optional.ofNullable(PropertyPrism.getInstanceOn(element))
        .map(PropertyPrism::value)
        .filter(Objects::nonNull)
        .map(Util::escapeQuotes)
        .orElse(namingConvention.from(fieldName));
    this.aliases =
      Optional.ofNullable(JsonAliasPrism.getInstanceOn(element))
        .map(JsonAliasPrism::value)
        .filter(Objects::nonNull)
        .stream()
        .flatMap(List::stream)
        .map(Util::escapeQuotes)
        .collect(Collectors.toList());

    if (raw) {
      genericType = GenericType.parse("java.lang.String");
      adapterShortType = "JsonAdapter<String>";
      adapterFieldName = "rawAdapter";
      defaultValue = "null";
      primitive = false;
    } else if (unmapped) {
      genericType = GenericType.parse("java.lang.Object");
      adapterShortType = "JsonAdapter<Object>";
      adapterFieldName = "objectJsonAdapter";
      defaultValue = "null";
      primitive = false;
    } else {
      genericType = GenericType.parse(rawType);
      final String shortType = genericType.shortType();
      primitive = PrimitiveUtil.isPrimitive(shortType);
      defaultValue = !primitive ? "null" : PrimitiveUtil.defaultValue(shortType);
      adapterShortType = initAdapterShortType(shortType);
      adapterFieldName = (primitive ? "p" : "") + initShortName();
    }
  }

  private String initAdapterShortType(String shortType) {
    String typeWrapped = "JsonAdapter<" + PrimitiveUtil.wrap(shortType) + ">";
    for (int i = 0; i < genericTypeParams.size(); i++) {
      String typeParam = genericTypeParams.get(i);
      if (typeWrapped.contains("<" + typeParam + ">")) {
        genericTypeParameter = true;
        genericTypeParamPosition = i;
        typeWrapped = typeWrapped.replace("<" + typeParam + ">", "<Object>");
      }
    }
    return typeWrapped;
  }

  private String initShortName() {
    if (genericTypeParameter) {
      final String name = genericType.shortName();
      return Util.initLower(name) + "JsonAdapterGeneric";
    }
    return Util.initLower(genericType.shortName()) + "JsonAdapter";
  }

  static String trimAnnotations(String type) {
    int pos = type.indexOf("@");
    if (pos == -1) {
      return type;
    }
    return type.substring(0, pos) + type.substring(type.lastIndexOf(' ') + 1);
  }

  void position(int pos) {
    position = pos;
  }

  String fieldName() {
    return fieldName;
  }

  String propertyName() {
    return propertyName;
  }

  boolean typeObjectBooleanWithIsPrefix() {
    return nameHasIsPrefix() && genericType.topType().equals("java.lang.Boolean");
  }

  boolean typeBooleanWithIsPrefix() {
    return nameHasIsPrefix() && (genericType.topType().equals("boolean") || genericType.topType().equals("java.lang.Boolean"));
  }

  private boolean nameHasIsPrefix() {
    return fieldName.length() > 2 && fieldName.startsWith("is") && Character.isUpperCase(fieldName.charAt(2));
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

  boolean includeForType(TypeSubTypeMeta subType) {
    return subTypes.containsKey(subType.type());
  }

  void addImports(Set<String> importTypes) {
    if (unmapped) {
      importTypes.add("java.util.*");
    }
    if (!raw) {
      genericType.addImports(importTypes);
    }
  }

  void cascadeTypes(Set<String> types) {
    if (!raw && !unmapped) {
      final String topType = genericType.topType();
      if ("java.util.List".equals(topType) || "java.util.Set".equals(topType)) {
        types.add(genericType.firstParamType());
      } else if ("java.util.Map".equals(topType)) {
        types.add(genericType.secondParamType());
      } else {
        types.add(topType);
      }
    }
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

  boolean isPublicField() {
    return publicField;
  }

  void writeDebug(Append writer) {
    writer.append("  // %s [%s] name:%s", fieldName, rawType, propertyName);
    if (!serialize) {
      writer.append(" ignoreSerialize");
    }
    if (!deserialize) {
      writer.append(" ignoreDeserialize");
    } else if (constructorParam) {
      writer.append(" constructor");
    } else if (setter != null) {
      writer.append(" setter:%s ", setter);
    } else if (publicField) {
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
    return genericType.shortType();
  }

  void writeField(Append writer) {
    writer.append("  private final %s %s;", adapterShortType, adapterFieldName).eol();
  }

  void writeConstructor(Append writer) {
    if (raw) {
      writer.append("    this.%s = jsonb.rawAdapter();", adapterFieldName).eol();
    } else {
      writer.append("    this.%s = jsonb.adapter(%s);", adapterFieldName, asTypeDeclaration()).eol();
    }
  }

  String asTypeDeclaration() {
    String asType = genericType.asTypeDeclaration().replace("? extends ", "");
    if (genericTypeParameter) {
      return genericTypeReplacement(asType, "param" + genericTypeParamPosition);
    }
    return asType;
  }

  private String genericTypeReplacement(String asType, String replaceWith) {
    String typeParam = genericTypeParams.get(genericTypeParamPosition);
    return asType.replace(typeParam + ".class", replaceWith);
  }

  void writeToJson(Append writer, String varName, String prefix) {
    if (unmapped) {
      writer.append("%sMap<String, Object> unmapped = ", prefix);
      writeGetValue(writer, varName, ";");
      writer.eol();
      writer.append("%sif (unmapped != null) {", prefix).eol();
      writer.append("%s for (Map.Entry<String, Object> entry : unmapped.entrySet()) {", prefix).eol();
      writer.append("%s   writer.name(entry.getKey());", prefix).eol();
      writer.append("%s   objectJsonAdapter.toJson(writer, entry.getValue());", prefix).eol();
      writer.append("%s }", prefix).eol();
      writer.append("%s}", prefix).eol();
    } else {
      writer.append("%swriter.name(%s);", prefix, position).eol();
      writer.append("%s%s.toJson(writer, ", prefix, adapterFieldName);
      writeGetValue(writer, varName, ");");
      writer.eol();
    }
  }

  private void writeGetValue(Append writer, String varName, String suffix) {
    if (getter != null) {
      writer.append("%s.%s()%s", varName, getter.getName(), suffix);
    } else if (publicField) {
      writer.append("%s.%s%s", varName, fieldName, suffix);
    } else {
      writer.append("FIXME: field %s is not public and has not getter ? ", fieldName);
    }
  }

  void writeFromJsonVariables(Append writer) {
    if (unmapped) {
      return;
    }
    writer.append("    %s _val$%s = %s;", pad(genericType.shortType()), fieldName, defaultValue);
    if (!constructorParam) {
      writer.append(" boolean _set$%s = false;", fieldName);
    }
    writer.eol();
  }

  void writeFromJsonVariablesRecord(Append writer) {
    final String type = genericTypeParameter ? "Object" : genericType.shortType();
    writer.append("    %s _val$%s = %s;", pad(type), fieldName, defaultValue).eol();
  }

  private String pad(String value) {
    final int pad = 10 - value.length();
    if (pad < 1) {
      return value;
    }
    final StringBuilder sb = new StringBuilder(10).append(value);
    for (int i = 0; i < pad; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

  void writeFromJsonSwitch(Append writer, boolean defaultConstructor, String varName, boolean caseInsensitiveKeys) {
    if (unmapped) {
      return;
    }
    if (aliases != null) {
      for (final String alias : aliases) {
        String propertyKey = caseInsensitiveKeys ? alias.toLowerCase() : alias;
        writer.append("        case \"%s\":", propertyKey).eol();
      }
    }
    String propertyKey = caseInsensitiveKeys ? propertyName.toLowerCase() : propertyName;
    writer.append("        case \"%s\": {", propertyKey).eol();
    if (!deserialize) {
      writer.append("          reader.skipValue();");
    } else if (defaultConstructor) {
      if (setter != null) {
        writer.append("          _$%s.%s(%s.fromJson(reader));", varName, setter.getName(), adapterFieldName);
      } else if (publicField) {
        writer.append("          _$%s.%s = %s.fromJson(reader);", varName, fieldName, adapterFieldName);
      }
    } else {
      writer.append("          _val$%s = %s.fromJson(reader);", fieldName, adapterFieldName);
      if (!constructorParam) {
        writer.append(" _set$%s = true;", fieldName);
      }
    }
    writer.append(" break;").eol();
    writer.append("        }").eol();
  }

  void writeFromJsonSetter(Append writer, String varName, String prefix) {
    if (unmapped) {
      writeFromJsonUnmapped(writer, varName);
      return;
    }
    if (setter != null) {
      writer.append("%s    if (_set$%s) _$%s.%s(_val$%s);", prefix, fieldName, varName, setter.getName(), fieldName).eol();
    } else if (publicField) {
      writer.append("%s    if (_set$%s) _$%s.%s = _val$%s;", prefix, fieldName, varName, fieldName, fieldName).eol();
    }
  }

  void writeFromJsonUnmapped(Append writer, String varName) {
    if (setter != null) {
      writer.append("    _$%s.%s(unmapped);", varName, setter.getName()).eol();
    } else if (publicField) {
      writer.append("    _$%s.%s = unmapped;", varName, fieldName).eol();
    }
  }

  void writeViewBuilder(Append writer, String shortName) {
    if (getter == null) {
      writer.append("    builder.add(\"%s\", %s, builder.field(%s.class, \"%s\"));", propertyName, adapterFieldName, shortName, fieldName).eol();
    } else {
      String topType = genericType.topType() + ".class";
      if (genericTypeParameter) {
        topType = genericTypeReplacement(topType, "Object.class");
      }
      writer.append("    builder.add(\"%s\", %s, builder.method(%s.class, \"%s\", %s));", propertyName, adapterFieldName, shortName, getter.getName(), topType).eol();
    }
  }
}
