package io.avaje.jsonb.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static io.avaje.jsonb.generator.APContext.*;

/**
 * A type with generic parameters and potentially nested.
 */
final class GenericType {

  private static final GenericTypeMap TYPE_MAP = new GenericTypeMap();

  /**
   * Trim off generic wildcard from the raw type if present.
   */
  static String trimWildcard(String rawType) {
    if (rawType.endsWith("<?>")) {
      return rawType.substring(0, rawType.length() - 3);
    } else {
      return rawType;
    }
  }

  private final String raw;
  private String mainType;

  private final List<GenericType> params = new ArrayList<>();

  /**
   * Create for top level type.
   */
  GenericType(String raw) {
    this.raw = raw;
  }

  /**
   * Create for parameter type.
   */
  GenericType() {
    this.raw = null;
  }

  /**
   * Return true if this is a generic type.
   */
  static boolean isGeneric(String raw) {
    return raw.contains("<");
  }

  /**
   * Parse and return as GenericType.
   */
  static GenericType parse(String raw) {
    raw = trimWildcard(raw);
    if (raw.indexOf('<') == -1) {
      return new GenericType(raw);
    }
    return new GenericTypeParser(raw).parse();
  }

  @Override
  public String toString() {
    return raw != null ? raw : mainType + '<' + params + '>';
  }

  void addImports(Set<String> importTypes) {
    final String type = trimExtends();
    if (includeInImports(type)) {
      importTypes.add(type);
    }
    for (GenericType param : params) {
      param.addImports(importTypes);
    }
  }

  private static boolean includeInImports(String type) {
    return type != null && !type.startsWith("java.lang.") && type.contains(".");
  }

  String shortType() {
    StringBuilder sb = new StringBuilder();
    writeShortType(sb);
    return sb.toString();
  }

  /**
   * Append the short version of the type (given the type and parameters are in imports).
   */
  void writeShortType(StringBuilder sb) {
    String main = Util.shortName(trimExtends());
    sb.append(main);
    final int paramCount = params.size();
    if (paramCount > 0) {
      sb.append("<");
      for (int i = 0; i < paramCount; i++) {
        if (i > 0) {
          sb.append(",");
        }
        params.get(i).writeShortType(sb);
      }
      sb.append(">");
    }
  }

  void writeType(String prefix, StringBuilder sb) {
    String main = Util.shortName(trimExtends());
    sb.append(prefix).append(main).append(".class");
    final int paramCount = params.size();
    if (paramCount > 0) {
      for (GenericType param : params) {
        param.writeType(",", sb);
      }
    }
  }

  String shortName() {
    StringBuilder sb = new StringBuilder();
    shortName(sb);
    return sb.toString().replace("[]", "Array");
  }

  void shortName(StringBuilder sb) {
    sb.append(Util.shortName(trimExtends()));
    for (GenericType param : params) {
      param.shortName(sb);
    }
  }

  private String trimExtends() {
    String type = topType();
    if (type != null && type.startsWith("? extends ")) {
      return type.substring(10);
    }
    return type;
  }

  String topType() {
    return (mainType != null) ? mainType : raw;
  }

  void setMainType(String mainType) {
    this.mainType = mainType;
  }

  void addParam(GenericType param) {
    params.add(param);
  }

  String asTypeDeclaration() {
    if (params.isEmpty()) {
      return asTypeBasic();
    }
    if (params.size() == 1) {
      var result = asTypeContainer();
      if (result != null) return result;
    }
    StringBuilder sb = new StringBuilder();
    writeType("Types.newParameterizedType(", sb);
    return sb.append(")").toString();
  }

  private String asTypeBasic() {
    String topType = topType();
    String adapterType = TYPE_MAP.typeOfRaw(topType);
    if (adapterType != null) {
      return adapterType;
    }
    return Util.shortName(topType)+".class";
  }

  private String asTypeContainer() {
    GenericType param = params.get(0);
    String containerType = topType();
    if (isAssignable(containerType, "java.util.List")) {
      return "Types.listOf(" + Util.shortName(param.topType()) + ".class)";
    }
    if (isAssignable(containerType, "java.util.Set")) {
      return "Types.setOf(" + Util.shortName(param.topType()) + ".class)";
    }
    if (isAssignable(containerType, "java.util.stream.Stream")) {
      return "Types.streamOf(" + Util.shortName(param.topType()) + ".class)";
    }
    if (isAssignable(containerType, "java.util.Optional")) {
      return "Types.optionalOf(" + Util.shortName(param.topType()) + ".class)";
    }
    return null;
  }

  String firstParamType() {
    return params.isEmpty() ? "java.lang.Object" : params.get(0).topType();
  }

  String secondParamType() {
    return params.size() != 2 ? "java.lang.Object" : params.get(1).topType();
  }
}
