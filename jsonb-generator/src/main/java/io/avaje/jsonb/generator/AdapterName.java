package io.avaje.jsonb.generator;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

final class AdapterName {
  final String shortName;
  final String adapterPackage;
  final String fullName;

  AdapterName(BeanReader beanReader) {
    String originPackage = APContext.elements().getPackageOf(beanReader.beanType()).getQualifiedName().toString();
    var name = shortName(beanReader.beanType());
    shortName = name.substring(0, name.length() - 1);
    if (beanReader.isPkgPrivate() || "".equals(originPackage)) {
      this.adapterPackage = originPackage;
    } else {
      this.adapterPackage = ProcessingContext.isImported(beanReader.beanType()) ? importedPkg(originPackage) : originPackage;
    }
    this.fullName =
      adapterPackage.isBlank()
        ? shortName + "JsonAdapter"
        : adapterPackage + "." + shortName + "JsonAdapter";
  }

  private String importedPkg(String originPackage) {
    return Optional.ofNullable(APContext.getProjectModuleElement())
            .filter(m -> !m.isUnnamed())
            .map(Element::getEnclosedElements)
            .map(l -> l.get(0).getSimpleName().toString())
            .orElse(originPackage)
        + ".jsonb";
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
