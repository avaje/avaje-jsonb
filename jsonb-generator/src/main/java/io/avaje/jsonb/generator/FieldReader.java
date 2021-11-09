package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Set;

class FieldReader {

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

  private MethodReader setter;
  private MethodReader getter;

  private boolean constructorParam;

  FieldReader(Element element, NamingConvention namingConvention) {
    this.element = element;
    this.fieldName = element.getSimpleName().toString();
    this.propertyName = PropertyReader.name(namingConvention, fieldName, element);
    this.rawType = element.asType().toString();
    this.genericType = GenericType.parse(rawType);
    this.publicField = element.getModifiers().contains(Modifier.PUBLIC);

    String shortType = genericType.shortType();
    primitive = PrimitiveUtil.isPrimitive(shortType);
    defaultValue = !primitive ? "null" : PrimitiveUtil.defaultValue(shortType);
    String typeWrapped = PrimitiveUtil.wrap(shortType);
    adapterShortType = "JsonAdapter<" + typeWrapped + ">";

    String typeShortName = genericType.shortName();
    adapterFieldName = (primitive ? "p" : "") + Character.toLowerCase(typeShortName.charAt(0)) + typeShortName.substring(1) + "JsonAdapter";
  }

  String getFieldName() {
    return fieldName;
  }

  void addImports(Set<String> importTypes) {
    genericType.addImports(importTypes);
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

  boolean isPublic() {
    return publicField;
  }

  void writeDebug(Append writer) {
    writer.append("  // %s [%s] name:%s", fieldName, rawType, propertyName);
    if (constructorParam) {
      writer.append(" constructor").eol();
    } else if (setter != null) {
      writer.append(" setter:%s ", setter).eol();
    } else if (publicField) {
      writer.append(" publicField").eol();
    } else {
      writer.append(" ERROR?? no constructor, setter and not a public field?").eol();
    }
  }

  String adapterShortType() {
    return genericType.shortType();
  }

  void writeField(Append writer) {
    genericType.shortType();
    writer.append("  private final %s %s;", adapterShortType, adapterFieldName).eol();
  }

  void writeConstructor(Append writer) {
    String asType = genericType.asTypeDeclaration();
    writer.append("    this.%s = jsonb.adapter(%s);", adapterFieldName, asType).eol();
  }

  void writeToJson(Append writer, String varName) {
    writer.append("    writer.name(\"%s\");", propertyName).eol();
    writer.append("    %s.toJson(writer, ", adapterFieldName);
    if (publicField) {
      writer.append("%s.%s);", varName, fieldName).eol();
    } else if (getter != null) {
      writer.append("%s.%s());", varName, getter.getName()).eol();
    } else {
      writer.append("FIXME: field is not public and has not getter ? ", varName).eol();
    }
  }

  void writeFromJsonVariables(Append writer) {
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
    writer.append("        case \"%s\": {", propertyName).eol();
    if (defaultConstructor) {
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

  void writeFromJsonSetter(Append writer, String varName) {
    if (setter != null) {
      writer.append("    if (_set$%s) _$%s.%s(_val$%s);", fieldName, varName, setter.getName(), fieldName).eol();
    } else if (publicField) {
      writer.append("    if (_set$%s) _$%s.%s = _val$%s;", fieldName, varName, fieldName, fieldName).eol();
    }
  }

}
