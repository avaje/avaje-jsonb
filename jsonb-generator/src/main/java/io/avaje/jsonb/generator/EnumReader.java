package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.ProcessingContext.useEnhancedSwitch;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class EnumReader implements BeanReader {

  private final TypeElement element;
  private final String shortName;
  private final Optional<String> adapterPackage;
  private final boolean pkgPrivate;
  private final List<Constant> constants = new ArrayList<>();

  EnumReader(TypeElement beanType) {
    this(beanType, null, Optional.empty());
  }

  EnumReader(TypeElement beanType, TypeElement mixInElement, Optional<String> adapterPackage) {
    this.element = beanType;
    this.shortName = shortName(beanType);
    this.adapterPackage = adapterPackage.filter(Predicate.not(String::isBlank));
    this.pkgPrivate = isPkgPrivate(beanType);
    final var mixInConstants = mixInConstants(beanType, mixInElement);
    for (final Element enumConst : enumConstants(beanType)) {
      final var name = enumConst.getSimpleName().toString();
      final var source = mixInConstants.getOrDefault(name, enumConst);
      constants.add(new Constant(name, source));
    }
  }

  /**
   * Return true if the enum has any constants with {@code @Json.Property}, {@code @Json.Alias} or
   * {@code @Json.Ignore}.
   */
  static boolean hasCustomizedConstants(TypeElement enumType) {
    return enumConstants(enumType).stream().anyMatch(EnumReader::isCustomized);
  }

  private static boolean isCustomized(Element enumConst) {
    return PropertyPrism.isPresent(enumConst)
        || AliasPrism.getOptionalOn(enumConst).isPresent()
        || IgnorePrism.getOptionalOn(enumConst).isPresent();
  }

  private static List<Element> enumConstants(TypeElement enumType) {
    final var list = new ArrayList<Element>();
    for (final Element enclosed : enumType.getEnclosedElements()) {
      if (enclosed.getKind() == ElementKind.ENUM_CONSTANT) {
        list.add(enclosed);
      }
    }
    return list;
  }

  private static Map<String, Element> mixInConstants(
      TypeElement beanType, TypeElement mixInElement) {
    if (mixInElement == null) {
      return Map.of();
    }
    if (mixInElement.getKind() != ElementKind.ENUM) {
      APContext.logError(
          mixInElement,
          "@Json.MixIn of enum %s must itself be an enum",
          beanType.getQualifiedName());
      return Map.of();
    }
    final var targetNames = new TreeSet<String>();
    enumConstants(beanType).forEach(e -> targetNames.add(e.getSimpleName().toString()));
    final var map = new HashMap<String, Element>();
    for (final Element mixInConst : enumConstants(mixInElement)) {
      final var name = mixInConst.getSimpleName().toString();
      if (!targetNames.contains(name)) {
        APContext.logError(
            mixInConst,
            "@Json.MixIn enum constant %s does not exist on %s",
            name,
            beanType.getQualifiedName());
      }
      map.put(name, mixInConst);
    }
    return map;
  }

  private static boolean isPkgPrivate(TypeElement type) {
    Element e = type;
    while (e instanceof TypeElement) {
      if (!e.getModifiers().contains(Modifier.PUBLIC)) {
        return true;
      }
      e = e.getEnclosingElement();
    }
    return false;
  }

  @Override
  public void read() {
    // nothing to read here
  }

  @Override
  public void cascadeTypes(Set<String> extraTypes) {
    // no types to cascade
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

  @Override
  public Optional<String> adapterPackage() {
    return adapterPackage;
  }

  @Override
  public boolean supportsViewBuilder() {
    return false;
  }

  @Override
  public boolean isPkgPrivate() {
    return pkgPrivate;
  }

  @Override
  public void writeImports(Append writer, String packageName) {
    final var importTypes = new TreeSet<>(List.of(
      "java.util.HashMap",
      "java.util.Map",
      element.asType().toString(),
      "io.avaje.json.JsonDataException",
      "io.avaje.json.PropertyNames",
      "io.avaje.json.JsonReader",
      "io.avaje.json.JsonWriter",
      "io.avaje.json.JsonAdapter",
      "io.avaje.jsonb.spi.Generated",
      Constants.JSONB));
    Util.writeImports(writer, importTypes, packageName);
  }

  @Override
  public void writeFields(Append writer) {
    writer
        .append("  private static final Map<String, %s> toEnum = new HashMap<>();", shortName)
        .eol()
        .eol();
    writer.append("  static {").eol();
    for (final Constant constant : constants) {
      if (constant.deserialize) {
        for (final String jsonName : constant.readNames()) {
          writer.append("    toEnum.put(\"%s\", %s.%s);", jsonName, shortName, constant.name).eol();
        }
      }
    }
    writer.append("  }").eol().eol();
    writer.append("  private final PropertyNames writeNames;").eol();
    writer.append("  private final PropertyNames readNames;").eol().eol();
  }

  @Override
  public void writeConstructor(Append writer) {
    final var writeNames =
        constants.stream()
            .filter(c -> c.serialize)
            .map(c -> "\"" + c.jsonName + "\"")
            .collect(joining(", "));
    final var readNames =
        constants.stream()
            .filter(c -> c.deserialize)
            .flatMap(c -> c.readNames().stream())
            .distinct()
            .map(n -> "\"" + n + "\"")
            .collect(joining(", "));
    writer.append("    this.writeNames = jsonb.properties(%s);", writeNames).eol();
    writer.append("    this.readNames = jsonb.properties(%s);", readNames).eol();
  }

  @Override
  public void writeToJson(Append writer) {
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public void toJson(JsonWriter writer, %s value) {", shortName).eol();
    writer.append("    if (value == null) {").eol();
    writer.append("      writer.nullValue();").eol();
    writer.append("      return;").eol();
    writer.append("    }").eol();
    writer.append("    switch (value) {").eol();
    final boolean enhanced = useEnhancedSwitch();
    var position = 0;
    for (final Constant constant : constants) {
      final String statement;
      if (constant.serialize) {
        statement = String.format("writer.value(writeNames, %d);", position++);
      } else {
        statement = String.format("throw new JsonDataException(\"%s.%s is ignored for serialization\");", shortName, constant.name);
      }
      if (enhanced) {
        writer.append("      case %s -> %s", constant.name, statement).eol();
      } else {
        writer.append("      case %s:", constant.name).eol();
        writer.append("        %s", statement).eol();
        if (constant.serialize) {
          writer.append("        return;").eol();
        }
      }
    }
    if (enhanced) {
      writer.append("      default -> writer.value(value.name());").eol();
    } else {
      writer.append("      default:").eol();
      writer.append("        writer.value(value.name());").eol();
    }
    writer.append("    }").eol();
    writer.append("  }").eol();
  }

  @Override
  public void writeFromJson(Append writer) {
    writer.eol();
    writer.append("  @Override").eol();
    writer.append("  public %s fromJson(JsonReader reader) {", shortName).eol();
    writer.append("    if (reader.isNullValue()) {").eol();
    writer.append("      return null;").eol();
    writer.append("    }").eol();
    writer.append("    final String value = reader.readString(readNames);").eol();
    writer.append("    final var enumConstant = toEnum.get(value);").eol();
    writer.append("    if (enumConstant == null) {").eol();
    writer
        .append(
            "      throw new JsonDataException(\"Unable to determine %s enum value for \" + value);",
            shortName)
        .eol();
    writer.append("    }").eol();
    writer.append("    return enumConstant;").eol();
    writer.append("  }").eol();
  }

  private static final class Constant {

    final String name;
    final String jsonName;
    final List<String> aliases;
    final boolean serialize;
    final boolean deserialize;

    Constant(String name, Element source) {
      this.name = name;
      this.jsonName =
          Util.escapeQuotes(
              PropertyPrism.getOptionalOn(source)
                  .map(PropertyPrism::value)
                  .filter(Predicate.not(String::isEmpty))
                  .orElse(name));
      this.aliases = AliasPrism.escapedAliases(source);
      final var ignore = IgnorePrism.getOptionalOn(source);
      this.serialize = ignore.map(IgnorePrism::serialize).orElse(true);
      this.deserialize = ignore.map(IgnorePrism::deserialize).orElse(true);
    }

    Set<String> readNames() {
      final var names = new LinkedHashSet<String>();
      names.add(jsonName);
      names.addAll(aliases);
      return names;
    }
  }
}
