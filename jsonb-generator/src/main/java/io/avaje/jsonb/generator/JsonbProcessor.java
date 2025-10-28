package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.asTypeElement;
import static io.avaje.jsonb.generator.APContext.logError;
import static io.avaje.jsonb.generator.APContext.logNote;
import static io.avaje.jsonb.generator.APContext.typeElement;
import static io.avaje.jsonb.generator.Constants.JSON;
import static io.avaje.jsonb.generator.Constants.JSON_IMPORT;
import static io.avaje.jsonb.generator.Constants.JSON_IMPORT_LIST;
import static io.avaje.jsonb.generator.Constants.JSON_MIXIN;
import static io.avaje.jsonb.generator.ProcessingContext.addImportedPrism;
import static io.avaje.jsonb.generator.ProcessingContext.createMetaInfWriterFor;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateModuleInfoReader;
import io.avaje.prism.GenerateUtils;

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

  private final Set<String> writtenTypes = new HashSet<>();
  private final Map<String, ComponentMetaData> privateMetaData = new HashMap<>();
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
    new ComponentReader(metaData, privateMetaData).read();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    if (round.errorRaised()) {
      return false;
    }
    APContext.setProjectModuleElement(annotations, round);
    readModule();
    getElements(round, ValuePrism.PRISM_TYPE).ifPresent(this::writeValueAdapters);
    getElements(round, JSON).ifPresent(this::writeAdapters);
    getElements(round, JSON_MIXIN).ifPresent(this::writeAdaptersForMixInTypes);
    getElements(round, JSON_IMPORT_LIST).ifPresent(this::writeAdaptersForImportedList);
    getElements(round, JSON_IMPORT).ifPresent(this::writeAdaptersForImported);
    getElements(round, "io.avaje.spi.ServiceProvider").ifPresent(this::registerSPI);

    metaData.fullName(false);
    cascadeTypes();
    getElements(round, CustomAdapterPrism.PRISM_TYPE).ifPresent(this::registerCustomAdapters);

    writeComponent(round.processingOver());
    return false;
  }

  // Optional because annotations are not guaranteed to exist
  private Optional<? extends Set<? extends Element>> getElements(RoundEnvironment round, String name) {
    return Optional.ofNullable(typeElement(name)).map(round::getElementsAnnotatedWith);
  }

  private void registerCustomAdapters(Set<? extends Element> elements) {
    for (final var typeElement : ElementFilter.typesIn(elements)) {
      var pkgPrivate = !typeElement.getModifiers().contains(Modifier.PUBLIC);
      var meta = pkgPrivate ? pkgPrivateMetaData(typeElement) : metaData;
      final var type = typeElement.getQualifiedName().toString();
      writtenTypes.add(type);
      if (isGenericJsonAdapter(typeElement)) {
        ElementFilter.fieldsIn(typeElement.getEnclosedElements()).stream()
          .filter(isStaticFactory())
          .findFirst()
          .ifPresentOrElse(
            x -> {},
            () -> logError(typeElement, "Generic adapters require a public static AdapterFactory FACTORY field"));

        meta.addFactory(type);
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

        typeElement.getInterfaces().stream()
            .filter(t -> t.toString().contains("io.avaje.json.JsonAdapter"))
            .findFirst()
            .ifPresent(t -> sourceTypes.add(UType.parse(t).param0().fullWithoutAnnotations()));

        meta.add(type);
      }
    }
  }

  private ComponentMetaData pkgPrivateMetaData(TypeElement typeElement) {
    var packageName = APContext.elements().getPackageOf(typeElement).getQualifiedName().toString();
    return privateMetaData.computeIfAbsent(packageName, k -> new ComponentMetaData());
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
    final var copy = new ArrayList<>(allReaders);
    allReaders.clear();

    final Set<String> extraTypes = new TreeSet<>();
    for (final BeanReader reader : copy) {
      reader.cascadeTypes(extraTypes);
    }
    for (final String type : extraTypes) {
      if (!ignoreType(type)) {
        final TypeElement element = typeElement(type);
        if (element != null
            && element.getKind() != ElementKind.ENUM
            && !JsonPrism.isPresent(element)) {
          ProcessingContext.cascadedType(type);
          writeAdapterForType(element);
        }
      }
    }
  }

  private boolean ignoreType(String type) {
    return type.indexOf('.') == -1
        || type.startsWith("java.")
        || type.startsWith("javax.")
        || sourceTypes.contains(type)
        || writtenTypes.contains(type);
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
      .flatMap(e -> ImportsPrism.getInstanceOn(e).value().stream())
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

  private void writeComponent(boolean processingOver) {
    if (processingOver) {
      try {
        if (!metaData.isEmpty()) {
          componentWriter.initialise(false);
          componentWriter.write();
        }

        for (var meta : privateMetaData.values()) {
          if (meta.isEmpty()) {
            continue;
          }
          var writer = new SimpleComponentWriter(meta);
          writer.initialise(true);
          writer.write();
        }
        writeMetaInf();
        ProcessingContext.validateModule();
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
    writeAdapter(typeElement, new ClassReader(typeElement, ""));
  }

  private void writeAdapterForImportedType(TypeElement importedType, TypeElement implementationType) {
    final ClassReader beanReader = new ClassReader(importedType, "@Json.Import of ");
    if (implementationType != null) {
      beanReader.setImplementationType(implementationType);
    }
    writeAdapter(importedType, beanReader);
  }

  private void writeAdapterForMixInType(TypeElement typeElement, TypeElement mixin) {
    final ClassReader beanReader = new ClassReader(typeElement, mixin, "@Json.Mixin of ");
    writeAdapter(typeElement, beanReader);
  }

  private void writeAdapter(TypeElement typeElement, BeanReader beanReader) {
    if (!writtenTypes.add(typeElement.toString())) {
      return;
    }
    beanReader.read();
    if (beanReader.nonAccessibleField()) {
      if (beanReader.hasJsonAnnotation() && !ProcessingContext.isCascadeType(typeElement)) {
        logError("Error JsonAdapter due to nonAccessibleField for %s ", beanReader);
      }
      logNote(typeElement, "Skipped writing JsonAdapter for %s due to non accessible fields", beanReader);
      return;
    }
    try {
      final SimpleAdapterWriter beanWriter = new SimpleAdapterWriter(beanReader);
      if (beanReader.isPkgPrivate()) {
        writeMeta(beanWriter, pkgPrivateMetaData(typeElement));
      } else {
        writeMeta(beanWriter, metaData);
      }
      beanWriter.write();
      allReaders.add(beanReader);
      sourceTypes.add(typeElement.getSimpleName().toString());
    } catch (final IOException e) {
      logError("Error writing JsonAdapter for %s %s", beanReader, e);
    }
  }

  private void writeMeta(final SimpleAdapterWriter beanWriter, ComponentMetaData meta) {
    meta.add(beanWriter.fullName());
    if (beanWriter.hasGenericFactory()) {
      meta.addFactory(beanWriter.fullName());
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

  private void writeMetaInf() throws IOException {
    var services = ProcessingContext.readExistingMetaInfServices();
    final FileObject fileObject = createMetaInfWriterFor(Constants.META_INF_COMPONENT);
    if (fileObject != null) {
      final Writer writer = fileObject.openWriter();
      writer.write(String.join("\n", services));
      writer.close();
    }
  }
}
