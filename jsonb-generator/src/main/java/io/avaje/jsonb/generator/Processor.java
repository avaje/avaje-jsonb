package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.Constants.JSON;
import static io.avaje.jsonb.generator.Constants.JSON_IMPORT;
import static io.avaje.jsonb.generator.Constants.JSON_MIXIN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes({JSON, JSON_IMPORT, JSON_MIXIN})
public final class Processor extends AbstractProcessor {

  private final ComponentMetaData metaData = new ComponentMetaData();
  private final List<BeanReader> allReaders = new ArrayList<>();
  private final Set<String> sourceTypes = new HashSet<>();
  private final Set<String> mixInImports = new HashSet<>();

  private ProcessingContext context;
  private SimpleComponentWriter componentWriter;
  private boolean readModuleInfo;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.context = new ProcessingContext(processingEnv);
    this.componentWriter = new SimpleComponentWriter(context, metaData);
  }

  /**
   * Read the existing metadata from the generated component (if exists).
   */
  private void readModule() {
    if (readModuleInfo) {
      return;
    }
    readModuleInfo = true;
    new ComponentReader(context, metaData).read();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    readModule();
    writeAdapters(round.getElementsAnnotatedWith(context.element(JSON)));
    writeAdaptersForMixInTypes(round.getElementsAnnotatedWith(context.element(JSON_MIXIN)));
    writeAdaptersForImported(round.getElementsAnnotatedWith(context.element(JSON_IMPORT)));
    initialiseComponent();
    cascadeTypes();
    writeComponent(round.processingOver());
    return false;
  }

  private void cascadeTypes() {
    while (!allReaders.isEmpty()) {
      cascadeTypesInner();
    }
  }

  private void cascadeTypesInner() {
    ArrayList<BeanReader> copy = new ArrayList<>(allReaders);
    allReaders.clear();

    Set<String> extraTypes = new TreeSet<>();
    for (BeanReader reader : copy) {
      reader.cascadeTypes(extraTypes);
    }
    for (String type : extraTypes) {
      if (!ignoreType(type)) {
        TypeElement element = context.element(type);
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

  /** Elements that have a {@code @Json.MixIn} annotation. */
  private void writeAdaptersForMixInTypes(Set<? extends Element> mixInElements) {

    for (final Element mixin : mixInElements) {

      final TypeMirror mirror = MixInPrism.getInstanceOn(mixin).value();
      final String importType = mirror.toString();
      final TypeElement element = (TypeElement) context.asElement(mirror);

      mixInImports.add(importType);
      writeAdapterForMixInType(element, context.element(mixin.asType().toString()));
    }
  }

  /** Elements that have a {@code @Json.Import} annotation. */
  private void writeAdaptersForImported(Set<? extends Element> importedElements) {

    for (final Element importedElement : importedElements) {
      for (final TypeMirror importType : ImportPrism.getInstanceOn(importedElement).value()) {
        // if imported by mixin annotation skip
        if (mixInImports.contains(importType.toString())) {
          continue;
        }

        final TypeElement element = (TypeElement) context.asElement(importType);

        writeAdapterForType(element);
      }
    }
  }

  private void initialiseComponent() {
    metaData.initialiseFullName();
    try {
      componentWriter.initialise();
    } catch (final IOException e) {
      context.logError("Error creating writer for JsonbComponent", e);
    }
  }

  private void writeComponent(boolean processingOver) {
    if (processingOver) {
      try {
        componentWriter.write();
        componentWriter.writeMetaInf();
      } catch (final IOException e) {
        context.logError("Error writing component", e);
      }
    }
  }

  /**
   * Read the beans that have changed.
   */
  private void writeAdapters(Set<? extends Element> beans) {
    for (final Element element : beans) {
      if (!(element instanceof TypeElement)) {
        context.logError("unexpected type [" + element + "]");
      } else {
        writeAdapterForType((TypeElement) element);
      }
    }
  }

  private void writeAdapterForType(TypeElement typeElement) {
    final BeanReader beanReader = new BeanReader(typeElement, context);
    writeAdapter(typeElement, beanReader);
  }

  private void writeAdapterForMixInType(TypeElement typeElement, TypeElement mixin) {
    final BeanReader beanReader = new BeanReader(typeElement, mixin, context);
    writeAdapter(typeElement, beanReader);
  }

  private void writeAdapter(TypeElement typeElement, BeanReader beanReader) {
    beanReader.read();
    if (beanReader.nonAccessibleField()) {
      if (beanReader.hasJsonAnnotation()) {
        context.logError("Error JsonAdapter due to nonAccessibleField for %s ", beanReader);
      }
      return;
    }
    try {
      final SimpleAdapterWriter beanWriter = new SimpleAdapterWriter(beanReader, context);
      metaData.add(beanWriter.fullName());
      if (beanWriter.hasGenericFactory()) {
        metaData.addFactory(beanWriter.fullName());
      }
      beanWriter.write();
      allReaders.add(beanReader);
      sourceTypes.add(typeElement.getSimpleName().toString());
    } catch (final IOException e) {
      context.logError("Error writing JsonAdapter for %s %s", beanReader, e);
    }
  }
}
