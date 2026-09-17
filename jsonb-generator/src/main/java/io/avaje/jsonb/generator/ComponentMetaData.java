package io.avaje.jsonb.generator;

import static java.util.function.Predicate.not;

import java.util.*;

final class ComponentMetaData {

  private final Map<String, String> allTypes = new LinkedHashMap<>();
  private final Map<String, String> factoryTypes = new LinkedHashMap<>();
  private final Map<String, String> withTypes = new LinkedHashMap<>();
  private String fullName;

  @Override
  public String toString() {
    return allTypes.values().toString();
  }

  boolean contains(String type) {
    return allTypes.containsValue(type);
  }

  void add(String type) {
    Optional.ofNullable(APContext.typeElement(type))
      .flatMap(CustomAdapterPrism::getOptionalOn)
      .filter(not(CustomAdapterPrism::global))
      .ifPresentOrElse(p -> addWithType(type), () -> addUnique(allTypes, type));
  }

  void addFactory(String fullName) {
    addUnique(factoryTypes, fullName);
  }

  void addWithType(String type) {
    addUnique(withTypes, type);
  }

  /**
   * Add the adapter/factory only if not already present. Nested class names are recovered from
   * the previous component's MetaData annotation with dots (eg {@code a.b.C.DJsonAdapter}) while
   * freshly generated ones use the dollar form (eg {@code a.b.C$DJsonAdapter}), so compare with
   * the dollar normalised away.
   */
  private static void addUnique(Map<String, String> typesByNormalizedName, String type) {
    typesByNormalizedName.putIfAbsent(normalize(type), type);
  }

  private static String normalize(String type) {
    return type.replace('$', '.');
  }

  void setFullName(String fullName) {
    this.fullName = fullName;
  }

  String fullName(boolean pkgPrivate) {
    if (fullName == null) {
      var everyType = new ArrayList<>(allTypes.values());
      everyType.addAll(factoryTypes.values());
      String topPackage = TopPackage.of(everyType);
      var defaultPackage =
        topPackage == null
          || !topPackage.contains(".")
          && APContext.getProjectModuleElement().isUnnamed()
          && APContext.elements().getPackageElement(topPackage) == null;
      if (!defaultPackage && !pkgPrivate && !topPackage.endsWith(".jsonb")) {
        topPackage += ".jsonb";
      }

      if (defaultPackage) {
        fullName = "GeneratedJsonComponent";
      } else if (pkgPrivate) {
        fullName = topPackage + "." + name(topPackage) + "JsonComponent";
      } else if (APContext.isTestCompilation()) {
        fullName = topPackage + ".TestJsonComponent";
      } else {
        fullName = topPackage + ".GeneratedJsonComponent";
      }
    }
    return fullName;
  }

  List<String> all() {
    return List.copyOf(allTypes.values());
  }

  List<String> allFactories() {
    return List.copyOf(factoryTypes.values());
  }

  List<String> withTypes() {
    return List.copyOf(withTypes.values());
  }

  /**
   * Return the package imports for the JsonAdapters and related types.
   */
  Collection<String> allImports() {
    Set<String> packageImports = new TreeSet<>();
    for (String adapterFullName : allTypes.values()) {
      packageImports.add(adapterFullName);

      final String className = Util.baseTypeOfAdapter(adapterFullName);
      final int $index = className.indexOf("$");
      packageImports.add($index != -1 ? className.substring(0, $index) : className);
    }

    packageImports.addAll(factoryTypes.values());
    packageImports.addAll(withTypes.values());
    return packageImports;
  }

  boolean isEmpty() {
    return allTypes.isEmpty() && factoryTypes.isEmpty();
  }

  static String name(String name) {
    if (name == null) {
      return null;
    }
    final int pos = name.lastIndexOf('.');
    if (pos > -1) {
      name = name.substring(pos + 1);
    }
    return camelCase(name).replaceFirst("Jsonb", "Generated");
  }

  private static String camelCase(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    boolean upper = true;
    for (char aChar : name.toCharArray()) {
      if (Character.isLetterOrDigit(aChar)) {
        if (upper) {
          aChar = Character.toUpperCase(aChar);
          upper = false;
        }
        sb.append(aChar);
      } else if (toUpperOn(aChar)) {
        upper = true;
      }
    }
    return sb.toString();
  }

  private static boolean toUpperOn(char aChar) {
    return aChar == ' ' || aChar == '-' || aChar == '_';
  }
}
