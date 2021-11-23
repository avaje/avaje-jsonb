package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;

class AdapterName {

  final String shortName;
  final String adapterPackage;
  final String fullName;

  AdapterName(TypeElement origin) {
    String originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    String originPackage = Util.packageOf(originName);
    this.adapterPackage = originPackage.equals("") ? "jsonb" : originPackage + ".jsonb";
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
