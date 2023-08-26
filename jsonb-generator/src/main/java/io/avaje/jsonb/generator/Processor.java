package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.ProcessingContext.*;
import static io.avaje.jsonb.generator.Constants.*;
import static io.avaje.jsonb.generator.ProcessingContext.asTypeElement;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@SupportedAnnotationTypes({
  CustomAdapterPrism.PRISM_TYPE,
  JSON,
  JSON_IMPORT,
  JSON_IMPORT_LIST,
  JSON_MIXIN,
  ValuePrism.PRISM_TYPE
})
public final class Processor extends AbstractProcessor {

  private final ComponentMetaData metaData = new ComponentMetaData();
  private final List<BeanReader> allReaders = new ArrayList<>();
  private final Set<String> sourceTypes = new HashSet<>();
  private final Set<String> mixInImports = new HashSet<>();
  private final Set<String> enumElements = new HashSet<>();

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

    ProcessingContext.findModule(annotations, round);
    readModule();
    writeAdapters(round.getElementsAnnotatedWith(element(JSON)));
    writeEnumAdapters(round.getElementsAnnotatedWith(element(ValuePrism.PRISM_TYPE)));
    writeAdaptersForMixInTypes(round.getElementsAnnotatedWith(element(JSON_MIXIN)));
    writeAdaptersForImportedList(round.getElementsAnnotatedWith(element(JSON_IMPORT_LIST)));
    writeAdaptersForImported(round.getElementsAnnotatedWith(element(JSON_IMPORT)));
    registerCustomAdapters(round.getElementsAnnotatedWith(element(CustomAdapterPrism.PRISM_TYPE)));
    initialiseComponent();
    cascadeTypes();
    writeComponent(round.processingOver());
    return false;
  }

  private void registerCustomAdapters(Set<? extends Element> elements) {
    for (final var typeElement : ElementFilter.typesIn(elements)) {
      final var type = typeElement.getQualifiedName().toString();
      if (CustomAdapterPrism.getInstanceOn(typeElement).isGeneric()) {
        ElementFilter.fieldsIn(typeElement.getEnclosedElements()).stream()
            .filter(isStaticFactory())
            .findFirst()
            .ifPresentOrElse(
                x -> {},
                () ->
                    logError(
                        typeElement,
                        "Generic adapters require a public static JsonAdapter.Factory FACTORY field"));

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
                () ->
                    logError(
                        typeElement,
                        "Non-Generic adapters must have a public constructor with a single Jsonb parameter"));

        metaData.add(type);
      }
    }
  }

  private static Predicate<VariableElement> isStaticFactory() {
    return v -> v.getModifiers().contains(Modifier.STATIC) && "FACTORY".equals(v.getSimpleName().toString());
  }

  private void writeEnumAdapters(Set<? extends Element> elements) {
    for (final ExecutableElement element : ElementFilter.methodsIn(elements)) {
      final var typeElement = (TypeElement) element.getEnclosingElement();
      if (typeElement.getKind() != ElementKind.ENUM) {
        logError("@Json.Value is only for enum methods at: " + typeElement);
      } else {
        writeEnumAdapterForType(typeElement, element);
      }
    }
  }

  private void writeEnumAdapterForType(TypeElement typeElement, ExecutableElement element) {
    if (!enumElements.add(typeElement.asType().toString())) {
      logError("@Json.Value can only be used once on a given enum methods at: " + typeElement);
    }
    writeAdapter(typeElement, new EnumReader(typeElement, element));
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
        final TypeElement element = element(type);
        if (cascadeElement(element)) {
          writeAdapterForType(element);
        }
      }
    }
  }

  private boolean cascadeElement(TypeElement element) {
    return element.getKind() != ElementKind.ENUM
      && !metaData.contains(adapterName(element));
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
      writeAdapterForMixInType(element, element(mixin.asType().toString()));
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
        return;
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
    final ClassReader beanReader = new ClassReader(typeElement);
    writeAdapter(typeElement, beanReader);
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
}
