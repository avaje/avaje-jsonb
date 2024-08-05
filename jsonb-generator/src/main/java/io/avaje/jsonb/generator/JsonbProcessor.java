package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.*;
import static io.avaje.jsonb.generator.ProcessingContext.addImportedPrism;
import static io.avaje.jsonb.generator.Constants.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateModuleInfoReader;
import io.avaje.prism.GenerateUtils;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@GenerateUtils
@GenerateAPContext
@GenerateModuleInfoReader
@SupportedAnnotationTypes({
  CustomAdapterPrism.PRISM_TYPE,
  JSON,
  JSON_IMPORT,
  JSON_IMPORT_LIST,
  JSON_MIXIN,
  ValuePrism.PRISM_TYPE,
  "io.avaje.spi.ServiceProvider"
})
public final class JsonbProcessor extends AbstractProcessor {

  private final ComponentMetaData metaData = new ComponentMetaData();
  private final List<BeanReader> allReaders = new ArrayList<>();
  private final Set<String> sourceTypes = new HashSet<>();
  private final Set<String> mixInImports = new HashSet<>();
  private final Set<String> valueElements = new HashSet<>();

  private SimpleComponentWriter componentWriter;
  private boolean readModuleInfo;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    ProcessingContext.init(processingEnv);
    this.componentWriter = new SimpleComponentWriter(metaData);
    // write a note in target so that other apts can know inject is running
    try {
      var file = APContext.getBuildResource("avaje-processors.txt");
      var addition = new StringBuilder();
      if (file.toFile().exists()) {
        var result = Stream.concat(Files.lines(file), Stream.of("avaje-jsonb-generator"))
          .distinct()
          .collect(joining("\n"));
        addition.append(result);
      } else {
        addition.append("avaje-jsonb-generator");
      }
      Files.writeString(file, addition, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    } catch (IOException e) {
      // not an issue worth failing over
    }
  }

  /**
   * Read the existing metadata from the generated component (if exists).
   */
  private void readModule() {
    if (readModuleInfo) {
      return;
    }
    readModuleInfo = true;
    new ComponentReader(metaData).read();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    APContext.setProjectModuleElement(annotations, round);
    readModule();
    getElements(round, ValuePrism.PRISM_TYPE).ifPresent(this::writeValueAdapters);
    getElements(round, JSON).ifPresent(this::writeAdapters);
    getElements(round, JSON_MIXIN).ifPresent(this::writeAdaptersForMixInTypes);
    getElements(round, JSON_IMPORT_LIST).ifPresent(this::writeAdaptersForImportedList);
    getElements(round, JSON_IMPORT).ifPresent(this::writeAdaptersForImported);
    getElements(round, CustomAdapterPrism.PRISM_TYPE).ifPresent(this::registerCustomAdapters);
    getElements(round, "io.avaje.spi.ServiceProvider").ifPresent(this::registerSPI);

    initialiseComponent();
    cascadeTypes();
    writeComponent(round.processingOver());
    return false;
  }

  // Optional because annotations are not guaranteed to exist
  private Optional<? extends Set<? extends Element>> getElements(RoundEnvironment round, String name) {
    return Optional.ofNullable(typeElement(name)).map(round::getElementsAnnotatedWith);
  }

  private void registerCustomAdapters(Set<? extends Element> elements) {
    for (final var typeElement : ElementFilter.typesIn(elements)) {
      final var type = typeElement.getQualifiedName().toString();
      if (isGenericJsonAdapter(typeElement)) {
        ElementFilter.fieldsIn(typeElement.getEnclosedElements()).stream()
          .filter(isStaticFactory())
          .findFirst()
          .ifPresentOrElse(
            x -> {},
            () -> logError(typeElement, "Generic adapters require a public static JsonAdapter.Factory FACTORY field"));

        metaData.addFactory(type);
      } else {
        ElementFilter.constructorsIn(typeElement.getEnclosedElements()).stream()
          .filter(m -> m.getModifiers().contains(Modifier.PUBLIC))
          .filter(m -> m.getParameters().size() == 1)
          .map(m -> m.getParameters().get(0).asType().toString())
          .map(Util::trimAnnotations)
          .filter("io.avaje.jsonb.Jsonb"::equals)
          .findAny()
          .ifPresentOrElse(
            x -> {},
            () -> logNote(typeElement, "Non-Generic adapters should have a public constructor with a single Jsonb parameter"));

        metaData.add(type);
      }
    }
  }

  private static boolean isGenericJsonAdapter(TypeElement typeElement) {
    return typeElement.getInterfaces().stream()
      .map(UType::parse)
      .filter(u -> u.full().contains("JsonAdapter"))
      .anyMatch(u -> u.param0().isGeneric());
  }

  private static Predicate<VariableElement> isStaticFactory() {
    return v -> v.getModifiers().contains(Modifier.STATIC) && "FACTORY".equals(v.getSimpleName().toString());
  }

  private void writeValueAdapters(Set<? extends Element> elements) {
    for (final ExecutableElement element : ElementFilter.methodsIn(elements)) {
      final var typeElement = (TypeElement) element.getEnclosingElement();
      validateValue(element, typeElement);
      writeAdapter(typeElement, new ValueReader(typeElement, element));
    }
  }

  private void validateValue(final ExecutableElement element, final TypeElement typeElement) {
    if (!valueElements.add(typeElement.asType().toString())) {
      logError(typeElement, "@Json.Value can only be used once on a given type");
    } else if (!element.getParameters().isEmpty()) {
      logError(element, "@Json.Value can only be used on methods with no parameters");
    }
    if (typeElement.getKind() == ElementKind.ENUM) {
      return;
    }
    var returnType = Util.trimAnnotations(element.getReturnType().toString());

    var methods =
      ElementFilter.methodsIn(typeElement.getEnclosedElements()).stream()
        .filter(CreatorPrism::isPresent);

    final var constructors =
      ElementFilter.constructorsIn(typeElement.getEnclosedElements()).stream();

    if (Stream.concat(methods, constructors)
      .filter(s -> s.getParameters().size() == 1)
      .map(s -> s.getParameters().get(0).asType().toString())
      .map(Util::trimAnnotations)
      .noneMatch(returnType::equals)) {

      logError(
        typeElement,
        "Missing constructor or @Json.Creator factory method with signature %s(%s value)",
        Util.shortName(typeElement.getQualifiedName().toString()),
        Util.shortName(returnType));
    }
  }

  private void cascadeTypes() {
    while (!allReaders.isEmpty()) {
      cascadeTypesInner();
    }
  }

  private void cascadeTypesInner() {
    final ArrayList<BeanReader> copy = new ArrayList<>(allReaders);
    allReaders.clear();

    final Set<String> extraTypes = new TreeSet<>();
    for (final BeanReader reader : copy) {
      reader.cascadeTypes(extraTypes);
    }
    for (final String type : extraTypes) {
      if (!ignoreType(type)) {
        final TypeElement element = typeElement(type);
        if (element != null && cascadeElement(element)) {
          writeAdapterForType(element);
        }
      }
    }
  }

  private boolean cascadeElement(TypeElement element) {
    return element.getKind() != ElementKind.ENUM && !metaData.contains(adapterName(element));
  }

  private String adapterName(TypeElement element) {
    return new AdapterName(element).fullName();
  }

  private boolean ignoreType(String type) {
    return type.indexOf('.') == -1
      || type.startsWith("java.")
      || type.startsWith("javax.")
      || sourceTypes.contains(type);
  }

  /**
   * Elements that have a {@code @Json.MixIn} annotation.
   */
  private void writeAdaptersForMixInTypes(Set<? extends Element> mixInElements) {
    for (final Element mixin : mixInElements) {
      final TypeMirror mirror = MixInPrism.getInstanceOn(mixin).value();
      final String importType = mirror.toString();
      final TypeElement element = asTypeElement(mirror);
      mixInImports.add(importType);
      writeAdapterForMixInType(element, typeElement(mixin.asType().toString()));
    }
  }

  private void writeAdaptersForImportedList(Set<? extends Element> imported) {
    imported.stream()
      .flatMap(e -> ImportListPrism.getInstanceOn(e).value().stream())
      .forEach(this::addImported);
  }

  /**
   * Elements that have a {@code @Json.Import} annotation.
   */
  private void writeAdaptersForImported(Set<? extends Element> importedElements) {
    importedElements.stream()
      .flatMap(e -> ImportPrism.getAllInstancesOn(e).stream().peek(p -> addImportedPrism(p, e)))
      .forEach(this::addImported);
  }

  private void addImported(ImportPrism importPrism) {
    for (final TypeMirror importType : importPrism.value()) {
      // if imported by mixin annotation skip
      if (mixInImports.contains(importType.toString())) {
        continue;
      }
      writeAdapterForImportedType(asTypeElement(importType), implementationType(importPrism));
    }
  }

  private static TypeElement implementationType(ImportPrism importPrism) {
    final TypeMirror implementationType = importPrism.implementation();
    if (!"java.lang.Void".equals(implementationType.toString())) {
      return asTypeElement(implementationType);
    }
    return null;
  }

  private void initialiseComponent() {
    metaData.initialiseFullName();
    try {
      componentWriter.initialise();
    } catch (final IOException e) {
      logError("Error creating writer for JsonbComponent", e);
    }
  }

  private void writeComponent(boolean processingOver) {
    if (processingOver) {
      try {
        componentWriter.write();
        componentWriter.writeMetaInf();
      } catch (final IOException e) {
        logError("Error writing component", e);
      } finally {
        ProcessingContext.clear();
      }
    }
  }

  /**
   * Read the beans that have changed.
   */
  private void writeAdapters(Set<? extends Element> beans) {
    for (final Element element : beans) {
      if (!(element instanceof TypeElement)) {
        logError("unexpected type [" + element + "]");
      } else {
        writeAdapterForType((TypeElement) element);
      }
    }
  }

  private void writeAdapterForType(TypeElement typeElement) {
    if (valueElements.contains(typeElement.toString())) {
      return;
    }
    writeAdapter(typeElement, new ClassReader(typeElement));
  }

  private void writeAdapterForImportedType(TypeElement importedType, TypeElement implementationType) {
    final ClassReader beanReader = new ClassReader(importedType);
    if (implementationType != null) {
      beanReader.setImplementationType(implementationType);
    }
    writeAdapter(importedType, beanReader);
  }

  private void writeAdapterForMixInType(TypeElement typeElement, TypeElement mixin) {
    final ClassReader beanReader = new ClassReader(typeElement, mixin);
    writeAdapter(typeElement, beanReader);
  }

  private void writeAdapter(TypeElement typeElement, BeanReader beanReader) {
    beanReader.read();
    if (beanReader.nonAccessibleField()) {
      if (beanReader.hasJsonAnnotation()) {
        logError("Error JsonAdapter due to nonAccessibleField for %s ", beanReader);
      }
      logNote("Skipped writing JsonAdapter for %s due to non accessible fields", beanReader);
      return;
    }
    try {
      final SimpleAdapterWriter beanWriter = new SimpleAdapterWriter(beanReader);
      metaData.add(beanWriter.fullName());
      if (beanWriter.hasGenericFactory()) {
        metaData.addFactory(beanWriter.fullName());
      }
      beanWriter.write();
      allReaders.add(beanReader);
      sourceTypes.add(typeElement.getSimpleName().toString());
    } catch (final IOException e) {
      logError("Error writing JsonAdapter for %s %s", beanReader, e);
    }
  }

  private void registerSPI(Set<? extends Element> beans) {
    ElementFilter.typesIn(beans).stream()
      .filter(this::isExtension)
      .map(TypeElement::getQualifiedName)
      .map(Object::toString)
      .forEach(ProcessingContext::addJsonSpi);
  }

  private boolean isExtension(TypeElement te) {
    return APContext.isAssignable(te, "io.avaje.jsonb.spi.JsonbExtension");
  }
}
