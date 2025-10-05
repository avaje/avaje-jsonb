package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.typeElement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

final class ComponentReader {

  private final ComponentMetaData componentMetaData;
  private final Map<String, ComponentMetaData> privateMetaData;
  private static final TypeMirror generatedComponentType =
      APContext.typeElement("io.avaje.jsonb.spi.GeneratedComponent").asType();
  private static final boolean isTestCompilation = APContext.isTestCompilation();
  ComponentReader(ComponentMetaData metaData, Map<String, ComponentMetaData> privateMetaData) {
    this.componentMetaData = metaData;
    this.privateMetaData = privateMetaData;
  }

  void read() {
    for (String fqn : ProcessingContext.readExistingMetaInfServices()) {
      if (isTestCompilation) {
        ProcessingContext.addJsonSpi(fqn);
      }
      final TypeElement moduleType = typeElement(fqn);

      if (isGeneratedComponent(moduleType)
          && (!isTestCompilation || fqn.endsWith("TestJsonComponent"))) {
        if (hasPublicComponents(moduleType)) {
          componentMetaData.setFullName(fqn);
          readMetaData(moduleType, componentMetaData);
        } else {
          // non-public adapters grouped by packageName
          var packageName =
              APContext.elements().getPackageOf(moduleType).getQualifiedName().toString();
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
        .or(() -> hasPublicJsonFactory(moduleType))
        .orElse(false);
  }

  private static Optional<Boolean> hasPublicJsonFactory(TypeElement moduleType) {
    return JsonFactoryPrism.getOptionalOn(moduleType).map(JsonFactoryPrism::value).stream()
        .flatMap(List::stream)
        .map(APContext::asTypeElement)
        .findFirst()
        .map(ComponentReader::hasPublicModifier);
  }

  private static boolean hasPublicModifier(TypeElement a) {
    return a.getModifiers().contains(Modifier.PUBLIC);
  }

  private static boolean isGeneratedComponent(TypeElement moduleType) {
    return moduleType != null
        && APContext.types().isSubtype(moduleType.asType(), generatedComponentType);
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
