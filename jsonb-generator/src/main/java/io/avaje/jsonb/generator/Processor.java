package io.avaje.jsonb.generator;

import io.avaje.jsonb.Json;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class Processor extends AbstractProcessor {

  private final ComponentMetaData metaData = new ComponentMetaData();
  private ProcessingContext context;
  private Elements elementUtils;
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
    this.elementUtils = processingEnv.getElementUtils();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(Json.class.getCanonicalName());
    //TODO: Json.Import ?
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
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    readModule();
    writeBeanAdapters(roundEnv.getElementsAnnotatedWith(Json.class));
    writeComponent(roundEnv.processingOver());
    return false;
  }

  private void writeComponent(boolean processingOver) {
    if (processingOver) {
      SimpleComponentWriter componentWriter = new SimpleComponentWriter(context, metaData);
      try {
        componentWriter.write();
      } catch (IOException e) {
        context.logError("Error writing component", e);
      }
    }
  }

  /**
   * Read the beans that have changed.
   */
  private void writeBeanAdapters(Set<? extends Element> beans) {
    for (Element element : beans) {
      if (!(element instanceof TypeElement)) {
        context.logError("unexpected type [" + element + "]");
      } else {
        TypeElement typeElement = (TypeElement) element;
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
  }

}
