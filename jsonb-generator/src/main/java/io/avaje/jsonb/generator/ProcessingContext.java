package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.filer;
import static io.avaje.jsonb.generator.APContext.getModuleInfoReader;
import static io.avaje.jsonb.generator.APContext.getProjectModuleElement;
import static io.avaje.jsonb.generator.APContext.jdkVersion;
import static io.avaje.jsonb.generator.APContext.logError;
import static io.avaje.jsonb.generator.APContext.logWarn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

final class ProcessingContext {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private static final class Ctx {
    private final Map<String, JsonPrism> importedJsonMap = new HashMap<>();
    private final Map<String, List<SubTypePrism>> importedSubtypeMap = new HashMap<>();
    private final Set<String> services = new TreeSet<>();
    private final boolean injectPresent;

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

  static boolean isImported(Element element) {
    var moduleName = APContext.getProjectModuleElement().getQualifiedName();
    return !APContext.elements().getModuleOf(element).getQualifiedName().contentEquals(moduleName);
  }

  static List<SubTypePrism> importedSubtypes(TypeElement type) {
    return CTX.get().importedSubtypeMap.getOrDefault(type.asType().toString(), List.of());
  }

  private static boolean buildPluginAvailable() {
    try {
      return APContext.getBuildResource("avaje-plugin-exists.txt").toFile().exists();
    } catch (final Exception e) {
      return false;
    }
  }

  static void validateModule() {
    var module = getProjectModuleElement();
    if (module != null && !module.isUnnamed()) {
      var injectPresent = CTX.get().injectPresent;

      try (var reader = getModuleInfoReader()) {
        var moduleInfo = new ModuleInfoReader(module, reader);

        moduleInfo.validateServices("io.avaje.jsonb.spi.JsonbExtension", CTX.get().services);

        boolean noInjectPlugin =
          injectPresent && !moduleInfo.containsOnModulePath("io.avaje.jsonb.plugin");

        var buildPluginAvailable = buildPluginAvailable();

        final var noDirectJsonb =
          moduleInfo.requires().stream()
            .noneMatch(r -> r.getDependency().getQualifiedName().contentEquals("io.avaje.jsonb"));

        if (noInjectPlugin && (!buildPluginAvailable || noDirectJsonb)) {
          logWarn(module, "`requires io.avaje.jsonb.plugin` must be explicitly added or else avaje-inject may fail to detect and wire the default Jsonb instance");
        }

      } catch (Exception e) {
        // can't read module
      }
    }
  }

  static void addJsonSpi(String spi) {
    CTX.get().services.add(spi);
  }

  static void clear() {
    CTX.remove();
    APContext.clear();
  }

  static Set<String> readExistingMetaInfServices() {
    var services = CTX.get().services;
    try (final var file =
           APContext.filer()
             .getResource(StandardLocation.CLASS_OUTPUT, "", Constants.META_INF_COMPONENT)
             .toUri()
             .toURL()
             .openStream();
         final var buffer = new BufferedReader(new InputStreamReader(file));) {

      String line;
      while ((line = buffer.readLine()) != null) {
        line.replaceAll("\\s", "").replace(",", "\n").lines().forEach(services::add);
      }
    } catch (Exception e) {
      // not a critical error
    }
    return services;
  }
}
