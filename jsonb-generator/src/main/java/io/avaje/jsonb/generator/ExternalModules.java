package io.avaje.jsonb.generator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

final class ExternalModules {

  private static final Set<String> ADAPTED_TYPES = new HashSet<>();

  private ExternalModules() {}

  static boolean contains(String type) {
    return ADAPTED_TYPES.contains(type);
  }

  static void readAdaptedTypes() {
    var currentModule = APContext.getProjectModuleElement();
    if (currentModule == null || currentModule.isUnnamed() || !ADAPTED_TYPES.isEmpty()) return;

    var jsonbExtensionType = APContext.typeElement("io.avaje.jsonb.spi.JsonbExtension");
    if (jsonbExtensionType == null) return;
    var extensionTypeMirror = jsonbExtensionType.asType();

    APContext.elements().getAllModuleElements().stream()
      .filter(m -> !m.isUnnamed())
      .filter(m -> !m.equals(currentModule))
      .filter(m -> !m.getQualifiedName().toString().startsWith("java."))
      .filter(m -> !m.getQualifiedName().toString().startsWith("jdk."))
      .flatMap(m -> m.getDirectives().stream())
      .filter(d -> d.getKind() == ModuleElement.DirectiveKind.PROVIDES)
      .map(d -> (ModuleElement.ProvidesDirective) d)
      .filter(d -> APContext.types().isAssignable(d.getService().asType(), extensionTypeMirror))
      .flatMap(d -> d.getImplementations().stream())
      .map(impl -> APContext.typeElement(impl.getQualifiedName().toString()))
      .filter(Objects::nonNull)
      .filter(ExternalModules::isGeneratedComponent)
      .forEach(ExternalModules::readAdaptedTypesFromComponent);
  }

  private static boolean isGeneratedComponent(TypeElement te) {
    var generatedType = APContext.typeElement("io.avaje.jsonb.spi.GeneratedComponent");
    return generatedType != null
      && APContext.types().isSubtype(te.asType(), generatedType.asType());
  }

  private static void readAdaptedTypesFromComponent(TypeElement component) {
    for (var mirror : component.getAnnotationMirrors()) {
      var prism = MetaDataPrism.getInstance(mirror);
      if (prism == null) continue;
      prism.value().stream()
        .map(TypeMirror::toString)
        .map(APContext::typeElement)
        .filter(Objects::nonNull)
        .forEach(ExternalModules::registerAdaptedType);
    }
  }

  private static void registerAdaptedType(TypeElement adapterType) {
    adapterType.getInterfaces().stream()
      .filter(t -> t.toString().contains("io.avaje.json.JsonAdapter"))
      .findFirst()
      .ifPresent(t -> {
        var adapted = UType.parse(t).param0().fullWithoutAnnotations();
        if (adapted != null && !GenericType.isGeneric(adapted)) {
          ADAPTED_TYPES.add(adapted);
        }
      });
  }
}
