package io.avaje.jsonb.generator;

import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

final class EnumReader implements BeanReader {

  private final ExecutableElement method;
  private final TypeElement enumType;
  private final String shortName;
  private final String type;
  private final Set<String> importTypes = new TreeSet<>();
  private final String returnTypeStr;
  private final GenericType genericType;
  private final String adapterShortType;

  EnumReader(TypeElement beanType, ExecutableElement e) {
    this.method = e;
    this.enumType = beanType;
    final TypeMirror returnType = e.getReturnType();
    this.returnTypeStr =  PrimitiveUtil.wrap(Util.shortType(returnType.toString()));
    this.type = Util.trimAnnotations(returnType.toString());
    this.shortName = shortName(beanType);

    genericType = GenericType.parse(returnTypeStr);
    final String shortType = genericType.shortType();
    adapterShortType = "JsonAdapter<" + PrimitiveUtil.wrap(Util.shortType(shortType)) + ">";
  }

  @Override
  public void read() {
    // nothing to read here
  }

  @Override
  public void cascadeTypes(Set<String> extraTypes) {
    // no extra cascade types on enums
  }

  @Override
  public String toString() {
    return enumType.toString();
  }

  @Override
  public String shortName() {
    return shortName;
  }

  @Override
  public TypeElement getBeanType() {
    return enumType;
  }

  private Set<String> importTypes() {
    importTypes.add(Constants.JSONB_WILD);
    importTypes.add(Constants.IOEXCEPTION);
    importTypes.add(Constants.JSONB_SPI);
    importTypes.add("java.util.EnumMap");
    importTypes.add("java.util.HashMap");
    importTypes.add("java.util.Map");
    if (Util.validImportType(type)) {
      importTypes.add(type);
    }
    importTypes.add(Constants.JSONB_SPI);
    importTypes.add(enumType.asType().toString());
    importTypes.add(method.getReturnType().toString());
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
  public void writeFields(Append writer) {
    writer.append("  private static final Map<%s, %s> toValue = new EnumMap<>(%s.class);", shortName, returnTypeStr, shortName).eol();
    writer.append("  private static final Map<%s, %s> toEnum = new HashMap<>();", returnTypeStr, shortName).eol();
    writer.append("  private final %s adapter;", adapterShortType).eol();
    writer.eol();
  }

  @Override
  public void writeConstructor(Append writer) {
    writer.append("    this.adapter = jsonb.adapter(%s);", genericType.asTypeDeclaration()).eol();
    writer.append("    if(!toValue.isEmpty()) return;").eol();
    writer.append("    for(final var enumConst : %s.values()) {", shortName).eol();
    writer.append("      var val = enumConst.%s();", method.getSimpleName()).eol();
    writer.append("      toValue.put(enumConst, val);").eol();
    writer.append("      if(toEnum.containsKey(val)) throw new IllegalArgumentException(\"Duplicate value \"+ val + \" from enum method %s. @Json.Value methods must return unique values\");", method.getSimpleName()).eol();
    writer.append("      toEnum.put(val, enumConst);").eol();
    writer.append("    }").eol();
  }

  @Override
  public void writeToJson(Append writer) {
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public void toJson(JsonWriter writer, %s value) {", shortName).eol();
    writer.append("    adapter.toJson(writer, toValue.get(value));").eol();
    writer.append("  }").eol();
  }

  @Override
  public void writeFromJson(Append writer) {
    final String varName = Util.initLower(shortName);
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) {", shortName, varName).eol();
    writer.append("    final var value = adapter.fromJson(reader);").eol();
    writer.append("    final var enumConstant = toEnum.get(value);").eol();
    writer.append("    if (enumConstant == null) ", varName).eol();
    writer.append("      throw new JsonDataException(\"Unable to determine %s enum value for \" + value);", shortName).eol();
    writer.append("    return enumConstant;").eol();
    writer.append("  }").eol();
  }

  @Override
  public boolean supportsViewBuilder() {
    return false;
  }
}
