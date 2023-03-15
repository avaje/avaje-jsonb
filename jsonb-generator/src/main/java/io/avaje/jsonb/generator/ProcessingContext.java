package io.avaje.jsonb.generator;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

final class ProcessingContext {

  private static final Float JDK_VERSION =
      Float.parseFloat(System.getProperty("java.specification.version"));

  private static final ThreadLocal<ProcessingEnvironment> ENV = new ThreadLocal<>();
  private static final ThreadLocal<Messager> MESSAGER = new ThreadLocal<>();
  private static final ThreadLocal<Filer> FILER = new ThreadLocal<>();
  private static final ThreadLocal<Elements> ELEMENT_UTILS = new ThreadLocal<>();
  private static final ThreadLocal<Types> TYPE_UTILS = new ThreadLocal<>();

  private ProcessingContext() {}

  public static void init(ProcessingEnvironment processingEnv) {
    ENV.set(processingEnv);
    MESSAGER.set(processingEnv.getMessager());
    FILER.set(processingEnv.getFiler());
    ELEMENT_UTILS.set(processingEnv.getElementUtils());
    TYPE_UTILS.set(processingEnv.getTypeUtils());
  }

  public static Float jdkVersion() {
    return JDK_VERSION;
  }

  /** Log an error message. */
  static void logError(Element e, String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  static void logError(String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  static void logWarn(String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  static void logDebug(String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  /** Create a file writer for the given class name. */
  static JavaFileObject createWriter(String cls) throws IOException {
    return FILER.get().createSourceFile(cls);
  }

  static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return FILER.get().createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

  static TypeElement element(String rawType) {
    return ELEMENT_UTILS.get().getTypeElement(rawType);
  }

  static Element asElement(TypeMirror returnType) {
    return TYPE_UTILS.get().asElement(returnType);
  }

  static ProcessingEnvironment env() {
    return ENV.get();
  }

  public static void clear() {
    ENV.remove();
    MESSAGER.remove();
    FILER.remove();
    ELEMENT_UTILS.remove();
    TYPE_UTILS.remove();
  }
}
