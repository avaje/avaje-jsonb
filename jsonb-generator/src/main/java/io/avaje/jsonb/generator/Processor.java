package io.avaje.jsonb.generator;

import io.avaje.jsonb.Json;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Processor extends AbstractProcessor {

  private final ComponentMetaData metaData = new ComponentMetaData();
  private final ImportReader importReader = new ImportReader();
  private ProcessingContext context;
  private SimpleComponentWriter componentWriter;
  private boolean readModuleInfo;

  public Processor() {
  }

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

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(Json.class.getCanonicalName());
    annotations.add(Json.Import.class.getCanonicalName());
    return annotations;
  }

  /**
   * Read the existing meta data from InjectModule (if found) and the factory bean (if exists).
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
    writeAdapters(round.getElementsAnnotatedWith(Json.class));
    writeAdaptersForImported(round.getElementsAnnotatedWith(Json.Import.class));
    initialiseComponent();
    writeComponent(round.processingOver());
    return false;
  }

  /**
   * Elements that have a {@code @Json.Import} annotation.
   */
  private void writeAdaptersForImported(Set<? extends Element> importedElements) {
    for (Element importedElement : importedElements) {
      List<String> importTypes = importReader.read(importedElement);
      for (String importType : importTypes) {
        TypeElement element = context.element(importType);
        if (element == null) {
          context.logError("Unable to find imported element " + importType);
        } else {
          writeAdapterForType(element);
        }
      }
    }
  }

  private void initialiseComponent() {
    metaData.initialiseFullName();
    try {
      componentWriter.initialise();
    } catch (IOException e) {
      context.logError("Error creating writer for JsonbComponent", e);
    }
  }

  private void writeComponent(boolean processingOver) {
    if (processingOver) {
      try {
        componentWriter.write();
        componentWriter.writeMetaInf();
      } catch (IOException e) {
        context.logError("Error writing component", e);
      }
    }
  }

  /**
   * Read the beans that have changed.
   */
  private void writeAdapters(Set<? extends Element> beans) {
    for (Element element : beans) {
      if (!(element instanceof TypeElement)) {
        context.logError("unexpected type [" + element + "]");
      } else {
        writeAdapterForType((TypeElement) element);
      }
    }
  }

  private void writeAdapterForType(TypeElement typeElement) {
    BeanReader beanReader = new BeanReader(typeElement, context);
    beanReader.read();
    try {
      SimpleBeanWriter beanWriter = new SimpleBeanWriter(beanReader, context);
      metaData.add(beanWriter.fullName());
      beanWriter.write();
    } catch (IOException e) {
      context.logError("Error writing JsonAdapter for " + beanReader, e);
    }
  }

}
