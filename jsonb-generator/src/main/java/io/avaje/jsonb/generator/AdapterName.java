package io.avaje.jsonb.generator;

import javax.lang.model.element.TypeElement;

final class AdapterName {

  final String shortName;
  final String adapterPackage;
  final String fullName;

  AdapterName(TypeElement origin) {
    String originPackage = APContext.elements().getPackageOf(origin).toString();
    var name = shortName(origin);
    shortName = name.substring(0, name.length() - 1);
    if ("".equals(originPackage)) {
      this.adapterPackage = "jsonb";
    } else {
      this.adapterPackage = ProcessingContext.isImported(origin) ? originPackage + ".jsonb" : originPackage;
    }
    this.fullName = adapterPackage + "." + shortName + "JsonAdapter";
  }

  private String shortName(TypeElement origin) {
    var sb = new StringBuilder();
    if (origin.getNestingKind().isNested()) {
      sb.append(shortName((TypeElement) origin.getEnclosingElement()));
    }
    return sb.append(Util.shortName(origin.getSimpleName().toString())).append("$").toString();
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
