package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class FieldReader {

  private final Map<String, TypeSubTypeMeta> subTypes = new LinkedHashMap<>();
  private final Element element;
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

  private MethodReader setter;
  private MethodReader getter;
  private int position;
  private boolean constructorParam;

  FieldReader(Element element, NamingConvention namingConvention, TypeSubTypeMeta subType) {
    this.element = element;
    addSubType(subType);
    this.fieldName = element.getSimpleName().toString();
    this.propertyName = PropertyReader.name(namingConvention, fieldName, element);
    this.rawType = element.asType().toString();
    this.publicField = element.getModifiers().contains(Modifier.PUBLIC);

    PropertyIgnoreReader ignoreReader = new PropertyIgnoreReader(element);
    this.unmapped = ignoreReader.unmapped();
    this.serialize = ignoreReader.serialize();
    this.deserialize = ignoreReader.deserialize();

    if (unmapped) {
      genericType = GenericType.parse("java.lang.Object");
      adapterShortType = "JsonAdapter<Object>";
      adapterFieldName = "objectJsonAdapter";
      defaultValue = "null";
      primitive = false;
    } else {
      genericType = GenericType.parse(rawType);
      String shortType = genericType.shortType();
      primitive = PrimitiveUtil.isPrimitive(shortType);
      defaultValue = !primitive ? "null" : PrimitiveUtil.defaultValue(shortType);
      String typeWrapped = PrimitiveUtil.wrap(shortType);
      adapterShortType = "JsonAdapter<" + typeWrapped + ">";
      adapterFieldName = (primitive ? "p" : "") + Util.initLower(genericType.shortName()) + "JsonAdapter";
    }
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

  boolean isUnmapped() {
    return unmapped;
  }

  boolean include() {
    return serialize || deserialize;
  }

  boolean includeFromJson() {
    return deserialize;
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
    genericType.addImports(importTypes);
  }

  void cascadeTypes(Set<String> types) {
    if (!unmapped) {
      String topType = genericType.topType();
      if (topType.equals("java.util.List") || topType.equals("java.util.Set")) {
        types.add(genericType.firstParamType());
      } else if (topType.equals("java.util.Map")) {
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
    } else {
      if (constructorParam) {
        writer.append(" constructor");
      } else if (setter != null) {
        writer.append(" setter:%s ", setter);
      } else if (publicField) {
        writer.append(" publicField");
      } else {
        writer.append(" ERROR?? no constructor, setter and not a public field?");
      }
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
    String asType = genericType.asTypeDeclaration();
    writer.append("    this.%s = jsonb.adapter(%s);", adapterFieldName, asType).eol();
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
    if (publicField) {
      writer.append("%s.%s%s", varName, fieldName, suffix);
    } else if (getter != null) {
      writer.append("%s.%s()%s", varName, getter.getName(), suffix);
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

  private String pad(String value) {
    int pad = 10 - value.length();
    if (pad < 1) {
      return value;
    }
    StringBuilder sb = new StringBuilder(10).append(value);
    for (int i = 0; i < pad; i++) {
      sb.append(" ");
    }
    return sb.toString();
  }

  void writeFromJsonSwitch(Append writer, boolean defaultConstructor, String varName) {
    if (unmapped) {
      return;
    }
    writer.append("        case \"%s\": {", propertyName).eol();
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
      writer.append("    builder.add(\"%s\", %s, builder.field(%s.class, \"%s\"));", fieldName, adapterFieldName, shortName, fieldName).eol();
    } else {
      String topType = genericType.topType();
      writer.append("    builder.add(\"%s\", %s, builder.method(%s.class, \"%s\", %s.class));", fieldName, adapterFieldName, shortName, getter.getName(), topType).eol();
    }
  }
}
