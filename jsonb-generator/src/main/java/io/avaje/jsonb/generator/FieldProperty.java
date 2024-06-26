package io.avaje.jsonb.generator;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class FieldProperty {

  private final boolean raw;
  private final boolean unmapped;
  private final String rawType;
  private final boolean publicField;
  private final String fieldName;
  private final List<String> genericTypeParams;

  private final GenericType genericType;
  private final String adapterFieldName;
  private final String adapterShortType;
  private final String defaultValue;
  private final boolean optional;
  private boolean genericTypeParameter;
  private int genericTypeParamPosition;
  private boolean constructorParam;
  private int position;
  private MethodReader getter;
  private MethodReader setter;

  FieldProperty(MethodReader methodReader) {
    this(methodReader.returnType(), false, false, new ArrayList<>(), false, methodReader.getName());
  }

  FieldProperty(TypeMirror asType, boolean raw, boolean unmapped, List<String> genericTypeParams,
                       boolean publicField, String fieldName) {
    this.raw = raw;
    this.unmapped = unmapped;
    this.publicField = publicField;
    this.fieldName = fieldName;
    this.rawType = Util.trimAnnotations(asType.toString());
    this.optional = rawType.startsWith("java.util.Optional");
    this.genericTypeParams = genericTypeParams;

    if (raw) {
      genericType = GenericType.parse("java.lang.String");
      adapterShortType = "JsonAdapter<String>";
      adapterFieldName = "rawAdapter";
      defaultValue = "null";
    } else if (unmapped) {
      genericType = GenericType.parse("java.lang.Object");
      adapterShortType = "JsonAdapter<Object>";
      adapterFieldName = "objectJsonAdapter";
      defaultValue = "null";
    } else {
      genericType = GenericType.parse(rawType);
      final String shortType = genericType.shortType();
      boolean primitive = PrimitiveUtil.isPrimitive(shortType);
      defaultValue = !primitive ? "null" : PrimitiveUtil.defaultValue(shortType);
      adapterShortType = initAdapterShortType(shortType);
      adapterFieldName = (primitive && !optional ? "p" : "") + initShortName();
    }
  }

  void setConstructorParam() {
    constructorParam = true;
  }

  void setPosition(int pos) {
    position = pos;
  }

  void setGetterMethod(MethodReader getter) {
    this.getter = getter;
  }

  void setSetterMethod(MethodReader setter) {
    this.setter = setter;
  }

  public MethodReader setter() {
    return setter;
  }

  boolean isConstructorParam() {
    return constructorParam;
  }

  boolean isPublicField() {
    return publicField;
  }

  String rawType() {
    return rawType;
  }

  String fieldName() {
    return fieldName;
  }

  String adapterFieldName() {
    return adapterFieldName;
  }

  GenericType genericType() {
    return genericType;
  }

  String shortType() {
    return genericType.shortType();
  }

  private String initAdapterShortType(String shortType) {
    String typeWrapped = "JsonAdapter<" + PrimitiveUtil.wrap(shortType) + ">";
    for (int i = 0; i < genericTypeParams.size(); i++) {
      final String typeParam = genericTypeParams.get(i);
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

  String typeParamToObject(String shortType) {
    for (final String typeParam : genericTypeParams) {
      if (shortType.contains("<" + typeParam + ">")) {
        shortType = shortType.replace("<" + typeParam + ">", "<Object>");
      }
    }
    return shortType;
  }


  boolean typeObjectBooleanWithIsPrefix() {
    return nameHasIsPrefix() && isObjectBoolean();
  }

  boolean typeBooleanWithIsPrefix() {
    return nameHasIsPrefix() && isBoolean();
  }

  private boolean isObjectBoolean() {
    return "java.lang.Boolean".equals(genericType.topType());
  }

  private boolean isBoolean() {
    return ("boolean".equals(genericType.topType()) || "java.lang.Boolean".equals(genericType.topType()));
  }

  private boolean nameHasIsPrefix() {
    return fieldName.length() > 2 && fieldName.startsWith("is") && Character.isUpperCase(fieldName.charAt(2));
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
        types.add(genericType.firstParamType());
        types.add(genericType.secondParamType());
      } else {
        types.add(topType);
      }
    }
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

  private String asTypeDeclaration() {
    final String asType = genericType.asTypeDeclaration().replace("? extends ", "");
    if (genericTypeParameter) {
      return genericTypeReplacement(asType, "param" + genericTypeParamPosition);
    }
    return asType;
  }

  private String genericTypeReplacement(String asType, String replaceWith) {
    final String typeParam = genericTypeParams.get(genericTypeParamPosition);
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
      throw new IllegalStateException("Field " + fieldName + " is inaccessible. Add a getter or make the field public or package-private.");
    }
  }

  void writeFromJsonVariables(Append writer, String num) {
    if (unmapped) {
      return;
    }
    final String shortType = typeParamToObject(genericType.shortType());
    writer.append("    %s _val$%s = %s;", pad(shortType), fieldName + num, defaultValue);
    if (!constructorParam && !optional) {
      writer.append(" boolean _set$%s = false;", fieldName + num);
    }
    writer.eol();
  }

  void writeFromJsonVariablesRecord(Append writer, String num) {
    final String type = genericTypeParameter ? "Object" : genericType.shortType();
    writer.append("    %s _val$%s = %s;", pad(type), fieldName + num, defaultValue).eol();
  }

  private String pad(String value) {
    final int pad = 10 - value.length();
    if (pad < 1) {
      return value;
    }
    return value + " ".repeat(pad);
  }

  void writeViewBuilder(Append writer, String shortName, String propertyName) {
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

  void writeFromJsonSetter(Append writer, String varName, String prefix, String num) {
    if (unmapped) {
      writeFromJsonUnmapped(writer, varName);
      return;
    }
    if (constructorParam) {
      return;
    }
    if (setter != null && optional) {
      writer.append("%s    _$%s.%s(_val$%s);", prefix, varName, setter.getName(), fieldName + num).eol();
    } else if (setter != null) {
      writer.append("%s    if (_set$%s) _$%s.%s(_val$%s);", prefix, fieldName + num, varName, setter.getName(), fieldName + num).eol();
    } else if (publicField) {
      writer.append("%s    if (_set$%s) _$%s.%s = _val$%s;", prefix, fieldName + num, varName, fieldName, fieldName + num).eol();
    }
  }

  void writeFromJsonUnmapped(Append writer, String varName) {
    if (setter != null) {
      writer.append("    _$%s.%s(unmapped);", varName, setter.getName()).eol();
    } else if (publicField) {
      writer.append("    _$%s.%s = unmapped;", varName, fieldName).eol();
    }
  }

  public void writeFromJsonSwitch(Append writer, String varName, boolean defaultConstructor) {
    if (defaultConstructor) {
      if (setter != null) {
        writer.append("          _$%s.%s(%s.fromJson(reader));", varName, setter.getName(), adapterFieldName);
      } else if (publicField) {
        writer.append("          _$%s.%s = %s.fromJson(reader);", varName, fieldName, adapterFieldName);
      }
    } else {
      writer.append("          _val$%s = %s.fromJson(reader);", fieldName, adapterFieldName);
      if (!constructorParam && !optional) {
        writer.eol().append("          _set$%s = true;", fieldName);
      }
    }
  }
}
