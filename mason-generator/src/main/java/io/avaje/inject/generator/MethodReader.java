package io.avaje.inject.generator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class MethodReader {

  private final ExecutableElement element;
  private final String methodName;
  private final List<MethodParam> params = new ArrayList<>();

  MethodReader(ProcessingContext context, ExecutableElement element, TypeElement beanType) {
    this.element = element;
    this.methodName = element.getSimpleName().toString();
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

  void addImports(Set<String> importTypes) {
    for (MethodParam param : params) {
      param.addImports(importTypes);
    }
  }


  public boolean isPublic() {
    return element.getModifiers().contains(Modifier.PUBLIC);
  }

  public boolean isNotPrivate() {
    return !element.getModifiers().contains(Modifier.PRIVATE);
  }

  static class MethodParam {

//    private final String named;
//    private final UtilType utilType;
//    private final String paramType;
//    private final GenericType genericType;
//    private final boolean nullable;
    private final String simpleName;
//    private int providerIndex;
//    private boolean requestParam;
//    private String requestParamName;

    MethodParam(VariableElement param) {
      this.simpleName = param.getSimpleName().toString();
//      this.named = Util.getNamed(param);
//      this.nullable = Util.isNullable(param);
//      this.utilType = Util.determineType(param.asType());
//      this.paramType = utilType.rawType();
//      this.genericType = GenericType.maybe(paramType);
    }

    String builderGetDependency(String builderName, boolean forFactory) {
      StringBuilder sb = new StringBuilder();
//      if (!forFactory && isGenericParam()) {
//        // passed as provider to build method
//        sb.append("prov").append(providerIndex).append(".get(");
//      } else {
//        sb.append(builderName).append(".").append(utilType.getMethod(nullable));
//      }
//      if (genericType == null) {
//        sb.append(Util.shortName(paramType)).append(".class");
//      } else if (isProvider()) {
//        sb.append(providerParam()).append(".class");
//      } else if (forFactory) {
//        sb.append(Util.shortName(genericType.topType())).append(".class");
//      }
//      if (named != null && !named.isEmpty()) {
//        sb.append(",\"").append(named).append("\"");
//      } else if (!isGenericParam() && utilType.allowsNamedQualifier()) {
//        // implied qualifier name, leading '!' means implied
//        sb.append(",\"!");
//        final String shortName = Util.shortName(paramType);
//        if (simpleName.endsWith(shortName)) {
//          sb.append(simpleName, 0, simpleName.length() - shortName.length());
//        } else {
//          sb.append(simpleName);
//        }
//        sb.append("\"");
//      }
//      sb.append(")");
      return sb.toString();
    }

//    String getDependsOn() {
//      return paramType;
//    }

    void addImports(Set<String> importTypes) {
//      if (genericType != null) {
//        importTypes.add(Constants.PROVIDER);
//        genericType.addImports(importTypes);
//      } else {
//        importTypes.add(paramType);
//      }
    }

    private String nm(String raw) {
      return Util.shortName(raw);
    }

  }
}
