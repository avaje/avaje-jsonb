package io.avaje.jsonb.generator;

import java.util.ArrayList;
import java.util.List;

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
}
