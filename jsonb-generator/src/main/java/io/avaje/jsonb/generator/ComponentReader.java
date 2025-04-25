package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.typeElement;
import static java.util.stream.Collectors.toList;

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

      if (moduleType != null
          && "io.avaje.jsonb.spi.GeneratedComponent"
              .equals(moduleType.getSuperclass().toString())) {
        var adapters =
            MetaDataPrism.getInstanceOn(moduleType).value().stream()
                .map(APContext::asTypeElement)
                .collect(toList());

        if (adapters.get(0).getModifiers().contains(Modifier.PUBLIC)) {
          componentMetaData.setFullName(fqn);
          adapters.stream().forEach(t -> readMetaData(moduleType, componentMetaData));

        } else {
          var packageName =
              APContext.elements().getPackageOf(moduleType).getQualifiedName().toString();
          var meta = privateMetaData.computeIfAbsent(packageName, k -> new ComponentMetaData());
          adapters.stream()
              .map(TypeElement::getQualifiedName)
              .map(Object::toString)
              .forEach(meta::add);
        }
      }
    }
  }

  /** Read the existing JsonAdapters from the MetaData annotation of the generated component. */
  private void readMetaData(TypeElement moduleType, ComponentMetaData meta) {
    for (final AnnotationMirror annotationMirror : moduleType.getAnnotationMirrors()) {

      final MetaDataPrism metaData = MetaDataPrism.getInstance(annotationMirror);
      final JsonFactoryPrism metaDataFactory = JsonFactoryPrism.getInstance(annotationMirror);

      if (metaData != null) {
        metaData.value().stream().map(TypeMirror::toString).forEach(meta::add);

      } else if (metaDataFactory != null) {
        metaDataFactory.value().stream().map(TypeMirror::toString).forEach(meta::addFactory);
      }
    }
  }
}
