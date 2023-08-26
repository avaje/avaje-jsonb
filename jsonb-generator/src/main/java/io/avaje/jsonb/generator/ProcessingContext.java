package io.avaje.jsonb.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

final class ProcessingContext {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private static int jdkVersion;
  private static boolean previewEnabled;

  private static final class Ctx {
    private final ProcessingEnvironment env;
    private final Messager messager;
    private final Filer filer;
    private final Elements elements;
    private final Types types;
    private final Map<String, JsonPrism> importedJsonMap = new HashMap<>();
    private final Map<String, List<SubTypePrism>> importedSubtypeMap = new HashMap<>();
    private final boolean injectPresent;
    private ModuleElement module;
    private boolean validated;

    Ctx(ProcessingEnvironment env) {
      this.env = env;
      this.messager = env.getMessager();
      this.filer = env.getFiler();
      this.elements = env.getElementUtils();
      this.types = env.getTypeUtils();
      this.injectPresent = elements.getTypeElement("io.avaje.inject.Component") != null;

    }
  }

  private ProcessingContext() {}

  static void init(ProcessingEnvironment processingEnv) {
    CTX.set(new Ctx(processingEnv));
    jdkVersion = processingEnv.getSourceVersion().ordinal();
    previewEnabled = jdkVersion >= 13 && initPreviewEnabled(processingEnv);
  }

  private static boolean initPreviewEnabled(ProcessingEnvironment processingEnv) {
    try {
      return (boolean) ProcessingEnvironment.class.getDeclaredMethod("isPreviewEnabled").invoke(processingEnv);
    } catch (final Throwable e) {
      return false;
    }
  }

  static boolean useEnhancedSwitch() {
    return jdkVersion() >= 14;
  }

  static int jdkVersion() {
    return jdkVersion;
  }

  public static boolean previewEnabled() {
    return previewEnabled;
  }

  /** Log an error message. */
  static void logError(Element e, String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  static void logError(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  static void logWarn(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  static void logWarn(Element e, String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
  }

  static void logNote(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  static void logDebug(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  /** Create a file writer for the given class name. */
  static JavaFileObject createWriter(String cls) throws IOException {
    return CTX.get().filer.createSourceFile(cls);
  }

  static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return CTX.get().filer.createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

  static boolean isAssignable2Interface(String type, String superType) {
    return type.equals(superType)
        || element(type).getInterfaces().stream().anyMatch(t -> t.toString().contains(superType));
  }

  static TypeElement element(String rawType) {
    return CTX.get().elements.getTypeElement(rawType);
  }

  static Element asElement(TypeMirror returnType) {
    return CTX.get().types.asElement(returnType);
  }

  static TypeElement asTypeElement(TypeMirror returnType) {
    return (TypeElement) asElement(returnType);
  }

  static ProcessingEnvironment env() {
    return CTX.get().env;
  }

  static void addImportedPrism(ImportPrism prism, Element element) {
    if (!prism.subtypes().isEmpty() && prism.value().size() > 1) {
      logError(element, "subtypes cannot be used when an import annotation imports more than one class");
      return;
    }
    final var json = CTX.get().importedJsonMap;
    final var subtypes = CTX.get().importedSubtypeMap;
    prism
        .value()
        .forEach(
            m -> {
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

  static void findModule(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    if (CTX.get().module == null) {
      CTX.get().module =
          annotations.stream()
              .map(roundEnv::getElementsAnnotatedWith)
              .flatMap(Collection::stream)
              .findAny()
              .map(ProcessingContext::getModuleElement)
              .orElse(null);
    }
  }

  static void validateModule(String fqn) {
    var module = CTX.get().module;
    if (module != null && !CTX.get().validated && !module.isUnnamed()) {

          var injectPresent = CTX.get().injectPresent;
      CTX.get().validated = true;
      try {
        var resource =
            CTX.get()
                .filer
                .getResource(StandardLocation.SOURCE_PATH, "", "module-info.java")
                .toUri()
                .toString();
        try (var inputStream = new URI(resource).toURL().openStream();
            var reader = new BufferedReader(new InputStreamReader(inputStream))) {

          AtomicBoolean noInjectPlugin = new AtomicBoolean(injectPresent);
          var noProvides =
              reader
                  .lines()
                  .map(
                      s -> {
                        if (injectPresent
                            && (s.contains("io.avaje.jsonb.plugin")
                                || s.contains("io.avaje.nima"))) {
                          noInjectPlugin.set(false);
                        }
                        return s;
                      })
                  .noneMatch(s -> s.contains(fqn));

          if (noProvides) {
            logError(
                module,
                "Missing `provides io.avaje.jsonb.Jsonb.GeneratedComponent with %s;`",
                fqn);
          }

          if (noInjectPlugin.get()) {
            logWarn(
                module,
                "`requires io.avaje.json.plugin` must be explicity added or else avaje-inject may fail to detect and wire the default Jsonb instance",
                fqn);
          }
        }
      } catch (Exception e) {
        // can't read module
      }
    }
  }

  static ModuleElement getModuleElement(Element e) {
    if (e == null || e instanceof ModuleElement) {
      return (ModuleElement) e;
    }
    return getModuleElement(e.getEnclosingElement());
  }

  static void clear() {
    CTX.remove();
  }
}
