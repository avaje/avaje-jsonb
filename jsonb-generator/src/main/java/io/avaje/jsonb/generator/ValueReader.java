package io.avaje.jsonb.generator;

import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

final class ValueReader implements BeanReader {

  private final ExecutableElement method;
  private final TypeElement element;
  private final String shortName;
  private final String type;
  private final Set<String> importTypes = new TreeSet<>();
  private final String returnTypeStr;
  private final GenericType genericType;
  private final String adapterShortType;
  private final boolean isEnum;
  private final ExecutableElement constructor;
  private final boolean pkgPrivate;

  ValueReader(TypeElement beanType, ExecutableElement e) {
    this.method = e;
    this.element = beanType;
    this.isEnum = beanType.getKind() == ElementKind.ENUM;
    final TypeMirror returnType = e.getReturnType();
    this.returnTypeStr = PrimitiveUtil.wrap(Util.shortType(returnType.toString()));
    this.type = Util.trimAnnotations(returnType.toString());
    this.shortName = shortName(beanType);

    genericType = GenericType.parse(returnType.toString());
    final String shortType = genericType.shortType();
    adapterShortType = "JsonAdapter<" + PrimitiveUtil.wrap(Util.shortType(shortType)) + ">";

    this.constructor = beanType.getEnclosedElements().stream()
      .filter(CreatorPrism::isPresent)
      .findFirst()
      .map(ExecutableElement.class::cast)
      .or(() -> ElementFilter.constructorsIn(element.getEnclosedElements()).stream()
        .filter(s -> s.getParameters().size() == 1)
        .filter(s -> Util.trimAnnotations(returnType.toString())
          .equals(Util.trimAnnotations(s.getParameters().get(0).asType().toString())))
        .findFirst())
      .orElse(null);

    this.pkgPrivate =
      !beanType.getModifiers().contains(Modifier.PUBLIC)
        || !e.getModifiers().contains(Modifier.PUBLIC)
        || !isEnum
        && constructor != null
        && !constructor.getModifiers().contains(Modifier.PUBLIC);
  }

  @Override
  public void read() {
    // nothing to read here
  }

  @Override
  public void cascadeTypes(Set<String> extraTypes) {
    extraTypes.add(returnTypeStr);
  }

  @Override
  public String toString() {
    return element.toString();
  }

  @Override
  public String shortName() {
    return shortName;
  }

  @Override
  public TypeElement beanType() {
    return element;
  }

  private Set<String> importTypes() {
    importTypes.add(Constants.IOEXCEPTION);
    importTypes.add("java.util.EnumMap");
    importTypes.add("java.util.HashMap");
    importTypes.add("java.util.Map");
    importTypes.add(type);
    importTypes.add(element.asType().toString());
    importTypes.add(method.getReturnType().toString());
    importTypes.add("io.avaje.json.JsonDataException");
    importTypes.add("io.avaje.json.PropertyNames");
    importTypes.add("io.avaje.json.JsonReader");
    importTypes.add("io.avaje.json.JsonWriter");
    importTypes.add("io.avaje.json.JsonAdapter");
    importTypes.add("io.avaje.jsonb.spi.Generated");
    importTypes.add(Constants.JSONB);
    return importTypes;
  }

  @Override
  public void writeImports(Append writer, String packageName) {
    for (final String importType : importTypes()) {
      if (Util.validImportType(importType, packageName)) {
        writer.append("import %s;", Util.sanitizeImports(importType)).eol();
      }
    }
    writer.eol();
  }

  @Override
  public void writeFields(Append writer) {
    if (isEnum) {
      writer.append("  private static final Map<%s, %s> toValue = new EnumMap<>(%s.class);", shortName, returnTypeStr, shortName).eol();
      writer.append("  private static final Map<%s, %s> toEnum = new HashMap<>();", returnTypeStr, shortName).eol();
    }
    writer.append("  private final %s adapter;", adapterShortType).eol().eol();
  }

  @Override
  public void writeConstructor(Append writer) {
    writer.append("    this.adapter = jsonb.adapter(%s);", genericType.asTypeDeclaration().replace("? extends ", "")).eol();
    if (isEnum) {
      writer.append("    if (toValue.isEmpty()) {").eol();
      writer.append("      for (final var enumConst : %s.values()) {", shortName).eol();
      writer.append("        var val = enumConst.%s();", method.getSimpleName()).eol();
      writer.append("        toValue.put(enumConst, val);").eol();
      writer.append("        if (toEnum.containsKey(val)) throw new IllegalArgumentException(\"Duplicate value \" + val + \" from enum method %s. @Json.Value methods must return unique values\");",method.getSimpleName()).eol();
      writer.append("        toEnum.put(val, enumConst);").eol();
      writer.append("      }").eol();
      writer.append("    }").eol();
    }
  }

  @Override
  public void writeToJson(Append writer) {
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public void toJson(JsonWriter writer, %s value) {", shortName).eol();
    if (isEnum) {
      writer.append("    adapter.toJson(writer, toValue.get(value));").eol();
    } else {
      writer.append("    adapter.toJson(writer, value.%s());", method.getSimpleName()).eol();
    }
    writer.append("  }").eol();
  }

  @Override
  public void writeFromJson(Append writer) {
    final String varName = Util.initLower(shortName);
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) {", shortName).eol();

    if (!isEnum) {
      var constructMethod =
        constructor.getKind() == ElementKind.CONSTRUCTOR
          ? "new " + shortName
          : shortName + "." + constructor.getSimpleName();
      writer.append("    return %s(adapter.fromJson(reader));", constructMethod).eol();

    } else {
      writer.append("    final var value = adapter.fromJson(reader);").eol();
      writer.append("    final var enumConstant = toEnum.get(value);").eol();
      writer.append("    if (enumConstant == null) ", varName).eol();
      writer.append("      throw new JsonDataException(\"Unable to determine %s enum value for \" + value);",shortName).eol();
      writer.append("    return enumConstant;").eol();
    }

    writer.append("  }").eol();
  }

  @Override
  public boolean supportsViewBuilder() {
    return false;
  }

  @Override
  public boolean isPkgPrivate() {
    return pkgPrivate;
  }
}
