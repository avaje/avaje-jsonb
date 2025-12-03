package io.avaje.jsonb.generator;

import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

final class ComponentMetaData {

  private final List<String> allTypes = new ArrayList<>();
  private final List<String> factoryTypes = new ArrayList<>();
  private final List<String> withTypes = new ArrayList<>();
  private String fullName;

  @Override
  public String toString() {
    return allTypes.toString();
  }

  boolean contains(String type) {
    return allTypes.contains(type);
  }

  void add(String type) {
    Optional.ofNullable(APContext.typeElement(type))
      .flatMap(CustomAdapterPrism::getOptionalOn)
      .filter(not(CustomAdapterPrism::global))
      .ifPresentOrElse(p -> withTypes.add(type), () -> allTypes.add(type));
  }

  void addFactory(String fullName) {
    factoryTypes.add(fullName);
  }

  void addWithType(String type) {
    withTypes.add(type);
  }

  void setFullName(String fullName) {
    this.fullName = fullName;
  }

  String fullName(boolean pkgPrivate) {
    if (fullName == null) {
      var everyType = new ArrayList<>(allTypes);
      everyType.addAll(factoryTypes);
      String topPackage = TopPackage.of(everyType);
      var defaultPackage =
        !topPackage.contains(".")
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
    return allTypes;
  }

  List<String> allFactories() {
    return factoryTypes;
  }

  List<String> withTypes() {
    return withTypes;
  }

  /**
   * Return the package imports for the JsonAdapters and related types.
   */
  Collection<String> allImports() {
    Set<String> packageImports = new TreeSet<>();
    for (String adapterFullName : allTypes) {
      packageImports.add(adapterFullName);

      final String className = Util.baseTypeOfAdapter(adapterFullName);
      final int $index = className.indexOf("$");
      packageImports.add($index != -1 ? className.substring(0, $index) : className);
    }

    packageImports.addAll(factoryTypes);
    packageImports.addAll(withTypes);
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
