package io.avaje.jsonb.generator;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read points for field injection and method injection
 * on baseType plus inherited injection points.
 */
class TypeExtendsInjection {

  private final List<MethodReader> publicConstructors = new ArrayList<>();
  private final List<FieldReader> allFields = new ArrayList<>();
  private final List<FieldReader> baseFields = new ArrayList<>();
  private final List<FieldReader> inheritedFields = new ArrayList<>();
  private final Map<String, MethodReader> maybeSetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> maybeGetterMethods = new LinkedHashMap<>();

  private final TypeElement baseType;
  private final ProcessingContext context;
  private final NamingConvention namingConvention;
  private MethodReader constructor;

  TypeExtendsInjection(TypeElement baseType, ProcessingContext context, NamingConvention namingConvention) {
    this.baseType = baseType;
    this.context = context;
    this.namingConvention = namingConvention;
  }

  void read(TypeElement type) {
    for (Element element : type.getEnclosedElements()) {
      switch (element.getKind()) {
        case CONSTRUCTOR:
          readConstructor(element, type);
          break;
        case FIELD:
          readField(element, type);
          break;
        case METHOD:
          readMethod(element, type);
          break;
      }
    }
  }

  private void readField(Element element, TypeElement type) {
    if (!element.getModifiers().contains(Modifier.TRANSIENT)) {
      if (type != baseType) {
        baseFields.add(new FieldReader(element, namingConvention));
      } else {
        inheritedFields.add(new FieldReader(element, namingConvention));
      }
    }
  }

  private void readConstructor(Element element, TypeElement type) {
    if (type != baseType) {
      // only interested in the top level constructors
      return;
    }
    ExecutableElement ex = (ExecutableElement) element;
    MethodReader methodReader = new MethodReader(context, ex, baseType).read();
    if (methodReader.isPublic()) {
      publicConstructors.add(methodReader);
    }
  }

  private void readMethod(Element element, TypeElement type) {
    ExecutableElement methodElement = (ExecutableElement) element;
    if (methodElement.getModifiers().contains(Modifier.PUBLIC)) {
      List<? extends VariableElement> parameters = methodElement.getParameters();
      final String methodKey = methodElement.getSimpleName().toString();
      if (parameters.size() == 1) {
        if (!maybeSetterMethods.containsKey(methodKey)) {
          MethodReader methodReader = new MethodReader(context, methodElement, type).read();
          maybeSetterMethods.put(methodKey, methodReader);
        }
      } else if (parameters.size() == 0) {
        if (!maybeGetterMethods.containsKey(methodKey)) {
          MethodReader methodReader = new MethodReader(context, methodElement, type).read();
          maybeGetterMethods.put(methodKey, methodReader);
        }
      }
    }
  }

  private final Map<String, MethodReader.MethodParam> constructorParamMap = new LinkedHashMap<>();

  void processCompleted() {
    constructor = determineConstructor();
    if (constructor != null) {
      List<MethodReader.MethodParam> params = constructor.getParams();
      for (MethodReader.MethodParam param : params) {
        constructorParamMap.put(param.name(), param);
      }
    }
    allFields.addAll(baseFields);
    allFields.addAll(inheritedFields);

    matchFieldsToSetterOrConstructor();
    matchFieldsToGetter();
  }

  private void matchFieldsToSetterOrConstructor() {
    for (FieldReader field : allFields) {
      if (constructorParamMap.get(field.getFieldName()) != null) {
        field.constructorParam();
      } else {
        matchFieldToSetter(field);
      }
    }
  }


  private void matchFieldToSetter(FieldReader field) {
    String name = field.getFieldName();
    MethodReader setter = maybeSetterMethods.get(name);
    if (setter != null) {
      field.setterMethod(setter);
    } else {
      setter = maybeSetterMethods.get(setterName(name));
      if (setter != null) {
        field.setterMethod(setter);
      } else if (!field.isPublic()) {
        context.logError("Non public field " + name + " with no matching setter or constructor?");
      }
    }
  }

  private void matchFieldsToGetter() {
    for (FieldReader field : allFields) {
      matchFieldToGetter(field);
    }
  }

  private void matchFieldToGetter(FieldReader field) {
    String name = field.getFieldName();
    MethodReader getter = maybeGetterMethods.get(name);
    if (getter != null) {
      field.getterMethod(getter);
      return;
    }
    getter = maybeGetterMethods.get(getterName(name));
    if (getter != null) {
      field.getterMethod(getter);
      return;
    }
    getter = maybeGetterMethods.get(isGetterName(name));
    if (getter != null) {
      field.getterMethod(getter);
      return;
    }
    if (!field.isPublic()) {
      context.logError("Non public field " + name + " with no matching getter?");
    }
  }

  private String setterName(String name) {
    return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  private String getterName(String name) {
    return "get" + Util.initcap(name);
  }

  private String isGetterName(String name) {
    return "is" + Util.initcap(name);
  }

  List<FieldReader> allFields() {
    return allFields;
  }

  MethodReader constructor() {
    return constructor;
  }

  private MethodReader determineConstructor() {
    if (publicConstructors.size() == 1) {
      return publicConstructors.get(0);
    }
    // check if there is only one public constructor
    List<MethodReader> allPublic = new ArrayList<>();
    for (MethodReader ctor : publicConstructors) {
      if (ctor.isPublic()) {
        allPublic.add(ctor);
      }
    }
    if (allPublic.size() == 1) {
      // fallback to the single public constructor
      return allPublic.get(0);
    }
    return null;
  }

}
