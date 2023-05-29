package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.ProcessingContext.useEnhancedSwitch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;

final class TypeSubTypeMeta {

  private final String type;
  private String name;
  private final String shortType;
  private TypeElement typeElement;
  private boolean defaultPublicConstructor;
  private final List<MethodReader> publicConstructors = new ArrayList<>();
  private final Set<String> constructorFieldNames = new LinkedHashSet<>();

  @Override
  public String toString() {
    return type;
  }

  TypeSubTypeMeta(SubTypePrism prism) {
    type = prism.type().toString();
    name = Util.escapeQuotes(prism.name());
    shortType = Util.shortType(type);
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
    if (name.isBlank()) {
      name = Util.shortType(type);
    }
    return name;
  }

  void addConstructor(MethodReader methodReader) {
    if (methodReader.getParams().isEmpty()) {
      defaultPublicConstructor = true;
    }
    publicConstructors.add(methodReader);
  }

  void writeFromJsonBuild(Append writer, String varName, SubTypeRequest req) {
    if (req.useSwitch()) {
      if (req.useEnum()) {
        writer.append("      case %s", name()).appendSwitchCase().eol();
      } else {
        writer.append("      case \"%s\"", name()).appendSwitchCase().eol();
      }
      writer.append("  ");
      writeFromJsonConstructor(writer, varName, req);
      writeFromJsonSetters(writer, varName, req);
      if (useEnhancedSwitch()) {
        writer.append("        yield _$%s;", varName).eol();
        writer.append("      }").eol();
      } else {
        writer.append("        return _$%s;", varName).eol();
      }
    } else {
      if (req.useEnum()) {
        writer.append("    if (%s.equals(%s)) {", name(), req.typeVar()).eol();
      } else {
        writer.append("    if (\"%s\".equals(%s)) {", name(), req.typeVar()).eol();
      }
      writeFromJsonConstructor(writer, varName, req);
      writeFromJsonSetters(writer, varName, req);
      writer.append("      return _$%s;", varName).eol();
      writer.append("    }").eol();
    }
  }

  private void writeFromJsonSetters(Append writer, String varName, SubTypeRequest req) {
    for (final FieldReader field : req.beanReader().allFields()) {
      if (isIncludeSetter(field)) {
        if (req.useSwitch()) {
          writer.append("  ");
        }
        field.writeFromJsonSetter(writer, varName, "  ");
      }
    }
  }

  private boolean isIncludeSetter(FieldReader field) {
    return field.includeFromJson() && !constructorFieldNames.contains(field.fieldName()) && field.includeForType(this);
  }


  private void writeFromJsonConstructor(Append writer, String varName, SubTypeRequest req) {
    writer.append("      %s _$%s = new %s(", shortType, varName, shortType);
    final MethodReader constructor = findConstructor();
    if (constructor != null) {
      final List<MethodReader.MethodParam> params = constructor.getParams();
      for (int i = 0, size = params.size(); i < size; i++) {
        if (i > 0) {
          writer.append(", ");
        }
        final var param = params.get(i);
        final String paramName = param.name();
        constructorFieldNames.add(paramName);
        var constructParamName = req.beanReader().constructorParamName(paramName);
        if (constructParamName.startsWith("_val$") && req.isCommonField(paramName)) {
          final var frequencySuffix = req.frequencySuffix(constructParamName);
          constructParamName = constructParamName + frequencySuffix;
        }

        writer.append(constructParamName); // assuming name matches field here?
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
