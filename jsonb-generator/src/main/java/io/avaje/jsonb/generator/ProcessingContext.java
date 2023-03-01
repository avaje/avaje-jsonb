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

  private static ProcessingEnvironment env;
  private static Messager messager;
  private static Filer filer;
  private static Elements elementUtils;
  private static Types typeUtils;

  public static void init(ProcessingEnvironment processingEnv) {
    env = processingEnv;
    messager = processingEnv.getMessager();
    filer = processingEnv.getFiler();
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
  }

  /**
   * Log an error message.
   */
  static void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  static void logError(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  static void logWarn(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  static void logDebug(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }

  /**
   * Create a file writer for the given class name.
   */
  static JavaFileObject createWriter(String cls) throws IOException {
    return filer.createSourceFile(cls);
  }

  static FileObject createMetaInfWriterFor(String interfaceType) throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, "", interfaceType);
  }

  static TypeElement element(String rawType) {
    return elementUtils.getTypeElement(rawType);
  }

  static Element asElement(TypeMirror returnType) {
    return typeUtils.asElement(returnType);
  }

  static ProcessingEnvironment env() {
    return env;
  }
}
