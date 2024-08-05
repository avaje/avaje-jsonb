package io.avaje.jsonb.generator;

import static java.util.function.Predicate.not;

import java.util.*;

final class ComponentMetaData {

  private final List<String> allTypes = new ArrayList<>();
  private final List<String> factoryTypes = new ArrayList<>();
  private final List<String> withTypes = new ArrayList<>();
  private String fullName;

  @Override
  public String toString() {
    return allTypes.toString();
  }

  /**
   * Ensure the component name has been initialised.
   */
  void initialiseFullName() {
    fullName();
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

  public void addWithType(String type) {
    withTypes.add(type);
  }

  void setFullName(String fullName) {
    this.fullName = fullName;
  }

  String fullName() {
    if (fullName == null) {
      String topPackage = TopPackage.of(allTypes);
      if (!topPackage.endsWith(".jsonb")) {
        topPackage += ".jsonb";
      }
      fullName = topPackage + ".GeneratedJsonComponent";
    }
    return fullName;
  }

  String packageName() {
    return Util.packageOf(fullName());
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

  public boolean isEmpty() {
    return allTypes.isEmpty() && factoryTypes.isEmpty();
  }
}
