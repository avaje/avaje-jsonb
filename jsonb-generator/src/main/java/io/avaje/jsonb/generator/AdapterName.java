package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;

final class AdapterName {

  final String shortName;
  final String adapterPackage;
  final String fullName;

  AdapterName(TypeElement origin) {
    String originName = origin.getQualifiedName().toString();
    String name = origin.getSimpleName().toString();
    String originPackage = Util.packageOf(originName);
    if (origin.getNestingKind().isNested()) {
      String parent = Util.shortName(originPackage);
      originPackage = Util.packageOf(originPackage);
      shortName = parent + "$" + name;
    } else {
      shortName = name;
    }
    var originPackageName = originPackage;
    if ("".equals(originPackageName)) {
      this.adapterPackage = "jsonb";
    } else {
      this.adapterPackage =
          ProcessingContext.isImported(origin) ? originPackageName + ".jsonb" : originPackageName;
    }
    this.fullName = adapterPackage + "." + shortName + "JsonAdapter";
  }

  String shortName() {
    return shortName;
  }

  String adapterPackage() {
    return adapterPackage;
  }

  String fullName() {
    return fullName;
  }
}
