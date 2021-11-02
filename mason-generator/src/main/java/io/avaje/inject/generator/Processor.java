package io.avaje.inject.generator;

import io.avaje.mason.JsonClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Processor extends AbstractProcessor {

  private ProcessingContext context;
  private Elements elementUtils;
  private List<BeanReader> readers = new ArrayList<>();

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
    annotations.add(JsonClass.class.getCanonicalName());
    //TODO: Json.Import ?
    return annotations;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<? extends Element> beans = roundEnv.getElementsAnnotatedWith(JsonClass.class);
    readChangedBeans(beans);
    // write(roundEnv.processingOver());
    return false;
  }

  /**
   * Read the beans that have changed.
   */
  private void readChangedBeans(Set<? extends Element> beans) {
    for (Element element : beans) {
      if (!(element instanceof TypeElement)) {
        context.logError("unexpected type [" + element + "]");
      } else {
        TypeElement typeElement = (TypeElement) element;
        BeanReader beanReader = new BeanReader(typeElement, context);
        beanReader.read();
        readers.add(beanReader);

        try {
          SimpleBeanWriter beanWriter = new SimpleBeanWriter(beanReader, context);
          beanWriter.write();
        } catch (IOException e) {
          context.logError("Error writing JsonAdapter for " + beanReader, e);
        }
      }
    }
  }


}
