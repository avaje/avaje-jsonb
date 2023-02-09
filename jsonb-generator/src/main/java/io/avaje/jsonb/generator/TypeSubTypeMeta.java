package io.avaje.jsonb.generator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.TypeElement;

final class TypeSubTypeMeta {

  private final String type;
  private String name;
  private TypeElement typeElement;
  private boolean defaultPublicConstructor;
  private final List<MethodReader> publicConstructors = new ArrayList<>();

  @Override
  public String toString() {
    return type;
  }

  public TypeSubTypeMeta(SubTypePrism prism) {
    type = prism.type().toString();
    name = Optional.of(Util.escapeQuotes(prism.name())).filter(s -> s.length() > 0).orElse(null);
  }

  void setElement(TypeElement element) {
    this.typeElement = element;
  }

  TypeElement element() {
    return typeElement;
  }

  String type() {
    return type;
  }

  String name() {
    if (name == null) {
      name = Util.shortName(type);
    }
    return name;
  }

  void addConstructor(MethodReader methodReader) {
    if (methodReader.getParams().isEmpty()) {
      defaultPublicConstructor = true;
    }
    publicConstructors.add(methodReader);
  }

  void writeFromJsonBuild(Append writer, String varName, BeanReader beanReader) {
    writer.append("    if (\"%s\".equals(type)) {", name()).eol();
    writeFromJsonConstructor(writer, varName, beanReader);
    writeFromJsonSetters(writer, varName, beanReader);
    writer.append("      return _$%s;", varName).eol();
    writer.append("    }").eol();
  }

  private void writeFromJsonSetters(Append writer, String varName, BeanReader beanReader) {
    for (FieldReader field : beanReader.allFields()) {
      if (isIncludeSetter(field)) {
        field.writeFromJsonSetter(writer, varName, "  ");
      }
    }
  }

  private boolean isIncludeSetter(FieldReader field) {
    return field.includeFromJson()
      && !constructorFieldNames.contains(field.fieldName())
      && field.includeForType(this);
  }

  private final Set<String> constructorFieldNames = new LinkedHashSet<>();

  private void writeFromJsonConstructor(Append writer, String varName, BeanReader beanReader) {
    writer.append("      %s _$%s = new %s(", type, varName, type);
    MethodReader constructor = findConstructor();
    if (constructor != null) {
      List<MethodReader.MethodParam> params = constructor.getParams();
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }
        String paramName = params.get(i).name();
        constructorFieldNames.add(paramName);
        writer.append(beanReader.constructorParamName(paramName)); // assuming name matches field here?
      }
    }
    writer.append(");").eol();
  }

  private MethodReader findConstructor() {
    if (defaultPublicConstructor || publicConstructors.isEmpty()) {
      return null;
    }
    return publicConstructors.get(0);
  }

}
