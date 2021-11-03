package io.avaje.jsonb.generator;

import java.util.*;

class ComponentMetaData {

  private final List<String> allTypes = new ArrayList<>();
  private String fullName;

  void add(String type) {
    allTypes.add(type);
  }

  void setFullName(String fullName) {
    this.fullName = fullName;
  }

  String fullName() {
    if (fullName == null) {
      String topPackage = TopPackage.of(allTypes);
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
    for (String allType : allTypes) {
      String adapterPackage = Util.packageOf(allType);
      packageImports.add(adapterPackage + ".*");
      String typePackage = Util.packageOf(adapterPackage);
      packageImports.add(typePackage + ".*");
    }
    return packageImports;
  }
}
