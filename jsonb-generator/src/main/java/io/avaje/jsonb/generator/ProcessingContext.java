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

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private static int jdkVersion;
  private static boolean previewEnabled;

  private static final class Ctx {
    private final ProcessingEnvironment env;
    private final Messager messager;
    private final Filer filer;
    private final Elements elements;
    private final Types types;

    Ctx(ProcessingEnvironment env) {
      this.env = env;
      this.messager = env.getMessager();
      this.filer = env.getFiler();
      this.elements = env.getElementUtils();
      this.types = env.getTypeUtils();
    }
  }

  private ProcessingContext() {}

  static void init(ProcessingEnvironment processingEnv) {
    CTX.set(new Ctx(processingEnv));
    jdkVersion = processingEnv.getSourceVersion().ordinal();
    previewEnabled = jdkVersion >= 13 ? initPreviewEnabled(processingEnv) : false;
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
    return element(type).getInterfaces().stream().anyMatch(t -> t.toString().contains(superType));
  }

  static TypeElement element(String rawType) {
    return CTX.get().elements.getTypeElement(rawType);
  }

  static Element asElement(TypeMirror returnType) {
    return CTX.get().types.asElement(returnType);
  }

  static ProcessingEnvironment env() {
    return CTX.get().env;
  }

  static void clear() {
    CTX.remove();
  }
}
