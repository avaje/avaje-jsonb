package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.typeElement;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

final class ComponentReader {

  private final ComponentMetaData componentMetaData;
  private final Map<String, ComponentMetaData> privateMetaData;

  ComponentReader(ComponentMetaData metaData, Map<String, ComponentMetaData> privateMetaData) {
    this.componentMetaData = metaData;
    this.privateMetaData = privateMetaData;
  }

  void read() {
    for (String fqn : ProcessingContext.readExistingMetaInfServices()) {
      final TypeElement moduleType = typeElement(fqn);

      if (isGeneratedComponent(moduleType)) {
        if (hasPublicComponents(moduleType)) {
          componentMetaData.setFullName(fqn);
          readMetaData(moduleType, componentMetaData);
        } else {
          // non-public adapters grouped by packageName
          var packageName = APContext.elements().getPackageOf(moduleType).getQualifiedName().toString();
          var meta = privateMetaData.computeIfAbsent(packageName, k -> new ComponentMetaData());
          readMetaData(moduleType, meta);
        }
      }
    }
  }

  private static boolean hasPublicComponents(TypeElement moduleType) {
    return MetaDataPrism.getInstanceOn(moduleType).value().stream()
      .map(APContext::asTypeElement)
      .findFirst()
      .map(ComponentReader::hasPublicModifier)
      .orElse(hasPublicJsonFactory(moduleType));
  }

  private static Boolean hasPublicJsonFactory(TypeElement moduleType) {
    return JsonFactoryPrism.getInstanceOn(moduleType).value().stream()
      .map(APContext::asTypeElement)
      .findFirst()
      .map(ComponentReader::hasPublicModifier)
      .orElse(false);
  }

  private static boolean hasPublicModifier(TypeElement a) {
    return a.getModifiers().contains(Modifier.PUBLIC);
  }

  private static boolean isGeneratedComponent(TypeElement moduleType) {
    return moduleType != null && "io.avaje.jsonb.spi.GeneratedComponent".equals(moduleType.getSuperclass().toString());
  }

  /** Read the existing JsonAdapters from the MetaData annotation of the generated component. */
  private static void readMetaData(TypeElement moduleType, ComponentMetaData meta) {
    for (final AnnotationMirror annotationMirror : moduleType.getAnnotationMirrors()) {

      final MetaDataPrism metaData = MetaDataPrism.getInstance(annotationMirror);
      if (metaData != null) {
        metaData.value().stream()
          .map(TypeMirror::toString)
          .forEach(meta::add);
      }

      final JsonFactoryPrism metaDataFactory = JsonFactoryPrism.getInstance(annotationMirror);
      if (metaDataFactory != null) {
        metaDataFactory.value().stream()
          .map(TypeMirror::toString)
          .forEach(meta::addFactory);
      }
    }
  }
}
