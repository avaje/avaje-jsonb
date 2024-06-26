package io.avaje.jsonb.generator;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

final class MethodReader {

  private final ExecutableElement element;
  private final String methodName;
  private final List<MethodParam> params = new ArrayList<>();

  MethodReader(ExecutableElement element) {
    this.element = element;
    this.methodName = element.getSimpleName().toString();
  }

  @Override
  public String toString() {
    return methodName;
  }

  TypeMirror returnType() {
    return element.getReturnType();
  }

  MethodReader read() {
    List<? extends VariableElement> ps = element.getParameters();
    for (VariableElement p : ps) {
      params.add(new MethodParam(p));
    }
    return this;
  }

  String getName() {
    return methodName;
  }

  List<MethodParam> getParams() {
    return params;
  }

  boolean isPublic() {
    return Util.isPublic(element);
  }

  boolean isProtected() {
    return element.getModifiers().contains(Modifier.PROTECTED);
  }

  String creationString() {
    var shortName = Util.shortName(((TypeElement) element.getEnclosingElement()).getQualifiedName().toString());
    if (element.getKind() == ElementKind.CONSTRUCTOR) {
      return String.format("new %s(", shortName);
    }
    return String.format("%s.%s(", shortName, element.getSimpleName());
  }

  ExecutableElement element() {
    return element;
  }

  static class MethodParam {

    private final String simpleName;
    private final String type;
    private final VariableElement element;

    MethodParam(VariableElement param) {
      this.simpleName = param.getSimpleName().toString();
      this.type = param.asType().toString();
      element = param;
    }

    String name() {
      return simpleName;
    }

    public String type() {
      return type;
    }

    public VariableElement element() {
      return element;
    }

  }
}
