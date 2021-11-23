package io.avaje.jsonb.generator;

import java.util.*;

class ComponentMetaData {

  private final List<String> allTypes = new ArrayList<>();
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
    allTypes.add(type);
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

  /**
   * Return the package imports for the JsonAdapters and related types.
   */
  Collection<String> allImports() {
    Set<String> packageImports = new TreeSet<>();
    for (String adapterFullName : allTypes) {
      packageImports.add(Util.packageOf(adapterFullName) + ".*");
      packageImports.add(Util.baseTypeOfAdapter(adapterFullName));
    }
    return packageImports;
  }
}
