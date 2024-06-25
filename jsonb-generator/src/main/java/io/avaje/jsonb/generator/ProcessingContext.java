package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.filer;
import static io.avaje.jsonb.generator.APContext.getModuleInfoReader;
import static io.avaje.jsonb.generator.APContext.getProjectModuleElement;
import static io.avaje.jsonb.generator.APContext.jdkVersion;
import static io.avaje.jsonb.generator.APContext.logError;
import static io.avaje.jsonb.generator.APContext.logWarn;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

final class ProcessingContext {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private static final class Ctx {
    private final Map<String, JsonPrism> importedJsonMap = new HashMap<>();
    private final Map<String, List<SubTypePrism>> importedSubtypeMap = new HashMap<>();
    private final boolean injectPresent;
    private boolean validated;

    Ctx(ProcessingEnvironment env) {
      this.injectPresent = env.getElementUtils().getTypeElement("io.avaje.inject.Component") != null;
    }
  }

  private ProcessingContext() {}

  static void init(ProcessingEnvironment processingEnv) {
    APContext.init(processingEnv);
    CTX.set(new Ctx(processingEnv));
  }

  static boolean useEnhancedSwitch() {
    return jdkVersion() >= 14;
  }

  static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return filer().createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

  static void addImportedPrism(ImportPrism prism, Element element) {
    if (!prism.subtypes().isEmpty() && prism.value().size() > 1) {
      logError(element, "subtypes cannot be used when an import annotation imports more than one class");
      return;
    }
    final var json = CTX.get().importedJsonMap;
    final var subtypes = CTX.get().importedSubtypeMap;
    prism.value().forEach(m -> {
      final var type = m.toString();
      json.put(type, prism.jsonSettings());
      subtypes.put(type, prism.subtypes());
    });
  }

  static Optional<JsonPrism> importedJson(TypeElement type) {
    return Optional.ofNullable(CTX.get().importedJsonMap.get(type.asType().toString()));
  }

  static List<SubTypePrism> importedSubtypes(TypeElement type) {
    return CTX.get().importedSubtypeMap.getOrDefault(type.asType().toString(), List.of());
  }

  private static boolean buildPluginAvailable() {
    return resourceExists("target/avaje-plugin-exists.txt")
      || resourceExists("build/avaje-plugin-exists.txt");
  }

  private static boolean resourceExists(String relativeName) {
    try {
      final String resource =
        filer()
          .getResource(StandardLocation.CLASS_OUTPUT, "", relativeName)
          .toUri()
          .toString()
          .replaceFirst("/target/classes", "")
          .replaceFirst("/build/classes/java/main", "");
      return Paths.get(new URI(resource)).toFile().exists();
    } catch (final Exception e) {
      return false;
    }
  }

  static void validateModule(String fqn) {
    var module = getProjectModuleElement();
    if (module != null && !CTX.get().validated && !module.isUnnamed()) {
      var injectPresent = CTX.get().injectPresent;
      CTX.get().validated = true;

      try (var reader = getModuleInfoReader()) {
        var moduleInfo = new ModuleInfoReader(module, reader);

        boolean noInjectPlugin =
          injectPresent && !moduleInfo.containsOnModulePath("io.avaje.jsonb.plugin");

        var noProvides =
          moduleInfo.provides().stream()
            .flatMap(s -> s.implementations().stream())
            .noneMatch(s -> s.contains(fqn));

        var buildPluginAvailable = buildPluginAvailable();
        if (noProvides && !buildPluginAvailable) {
          logError(module, "Missing `provides io.avaje.jsonb.spi.JsonbExtension with %s;`", fqn);
        }

        final var noDirectJsonb =
          moduleInfo.requires().stream()
            .noneMatch(r -> r.getDependency().getQualifiedName().contentEquals("io.avaje.jsonb"));

        if (noInjectPlugin && (!buildPluginAvailable || noDirectJsonb)) {
          logWarn(module, "`requires io.avaje.jsonb.plugin` must be explicity added or else avaje-inject may fail to detect and wire the default Jsonb instance", fqn);
        }

      } catch (Exception e) {
        // can't read module
      }
    }
  }

  static void clear() {
    CTX.remove();
    APContext.clear();
  }
}
