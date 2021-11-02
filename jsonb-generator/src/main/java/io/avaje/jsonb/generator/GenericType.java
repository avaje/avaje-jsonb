package io.avaje.jsonb.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A type with generic parameters and potentially nested.
 */
class GenericType {

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

  String raw() {
    return raw;
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

  /**
   * Parse and return the main type if it contains a type parameter like {@code <T>}.
   */
  static String removeParameter(String raw) {
    final GenericType type = parse(raw);
    return type.hasParameter() ? type.getMainType() : raw;
  }

  /**
   * Parse and return as GenericType or null if it is not generic.
   */
  static GenericType maybe(String paramType) {
    return isGeneric(paramType) ? parse(paramType) : null;
  }

  @Override
  public String toString() {
    return raw != null ? raw : mainType + '<' + params + '>';
  }

  /**
   * Return true if the type contains a type parameter like {@code <T>}.
   */
  boolean hasParameter() {
    if (mainType != null && mainType.indexOf('.') == -1) {
      return true;
    }
    for (GenericType param : params) {
      if (param.hasParameter()) {
        return true;
      }
    }
    return false;
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

  /**
   * Append the short version of the type (given the type and parameters are in imports).
   */
  void writeShort(Append writer) {
    String main = Util.shortName(trimExtends());
    writer.append(main);
    final int paramCount = params.size();
    if (paramCount > 0) {
      writer.append("<");
      for (int i = 0; i < paramCount; i++) {
        if (i > 0) {
          writer.append(",");
        }
        params.get(i).writeShort(writer);
      }
      writer.append(">");
    }
  }

  void writeType(String prefix, StringBuilder sb) {
    String main = Util.shortName(trimExtends());
    sb.append(prefix).append(main).append(".class");
    final int paramCount = params.size();
    if (paramCount > 0) {
      for (int i = 0; i < paramCount; i++) {
        sb.append(",");
        params.get(i).writeType(",", sb);
      }
    }
  }

  String shortName() {
    StringBuilder sb = new StringBuilder();
    shortName(sb);
    return sb.toString();
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

  /**
   * Return the main type.
   */
  String getMainType() {
    return mainType;
  }

  /**
   * Return the parameter types.
   */
  List<GenericType> getParams() {
    return params;
  }

  void setMainType(String mainType) {
    this.mainType = mainType;
  }

  void addParam(GenericType param) {
    params.add(param);
  }

  String asTypeDeclaration() {
    if (params.size() == 0) {
      return asTypeBasic();
    }
    if (params.size() == 1) {
      return asTypeContainer();
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
    switch (containerType) {
      case "java.util.List":
        return "Types.listOf(" + Util.shortName(param.topType()) + ".class)";
      case "java.util.Set":
        return "Types.setOf(" + Util.shortName(param.topType()) + ".class)";
    }
    return "FIXME: Unhandled Container Type " + containerType;
  }
}
