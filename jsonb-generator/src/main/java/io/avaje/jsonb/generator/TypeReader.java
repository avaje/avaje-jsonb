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
class TypeReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";

  private final List<MethodReader> publicConstructors = new ArrayList<>();
  private final List<FieldReader> allFields = new ArrayList<>();
  private final Map<String, MethodReader> maybeSetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> maybeGetterMethods = new LinkedHashMap<>();

  private final TypeSubTypeReader subTypes;
  private final TypeElement baseType;
  private final ProcessingContext context;
  private final NamingConvention namingConvention;
  private MethodReader constructor;
  private boolean defaultPublicConstructor;
  private final Map<String, MethodReader.MethodParam> constructorParamMap = new LinkedHashMap<>();
  private TypeSubTypeMeta currentSubType;

  TypeReader(TypeElement baseType, ProcessingContext context, NamingConvention namingConvention) {
    this.baseType = baseType;
    this.context = context;
    this.namingConvention = namingConvention;
    this.subTypes = new TypeSubTypeReader(baseType, context);
  }

  void read(TypeElement type, TypeElement matchType) {
    final List<FieldReader> localFields = new ArrayList<>();
    for (Element element : type.getEnclosedElements()) {
      switch (element.getKind()) {
        case CONSTRUCTOR:
          readConstructor(element, type, matchType);
          break;
        case FIELD:
          readField(element, localFields);
          break;
        case METHOD:
          readMethod(element, type);
          break;
      }
    }
    if (currentSubType != null) {
      allFields.addAll(localFields);
    } else {
      if (type != baseType) {
        allFields.addAll(0, localFields);
      } else {
        allFields.addAll(localFields);
      }
    }
  }

  private void readField(Element element, List<FieldReader> localFields) {
    if (!element.getModifiers().contains(Modifier.TRANSIENT)) {
      localFields.add(new FieldReader(element, namingConvention, currentSubType));
    }
  }

  private void readConstructor(Element element, TypeElement type, TypeElement matchType) {
    if (type != matchType) {
      // only interested in the top level constructors
      return;
    }
    ExecutableElement ex = (ExecutableElement) element;
    MethodReader methodReader = new MethodReader(context, ex, baseType).read();
    if (methodReader.isPublic()) {
      if (currentSubType != null) {
        currentSubType.addConstructor(methodReader);
      } else {
        if (methodReader.getParams().isEmpty()) {
          defaultPublicConstructor = true;
        }
        publicConstructors.add(methodReader);
      }
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
      } else if (!field.isPublicField()) {
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
    if (!field.isPublicField()) {
      context.logError("Non public field " + name + " with no matching getter?");
    }
  }

  private String setterName(String name) {
    return "set" + Util.initCap(name);
  }

  private String getterName(String name) {
    return "get" + Util.initCap(name);
  }

  private String isGetterName(String name) {
    return "is" + Util.initCap(name);
  }

  List<FieldReader> allFields() {
    return allFields;
  }

  MethodReader constructor() {
    return constructor;
  }

  private MethodReader determineConstructor() {
    if (defaultPublicConstructor) {
      return null;
    }
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

  void process() {
    String base = baseType.getQualifiedName().toString();
    if (!GenericType.isGeneric(base)) {
      read(baseType, baseType);
    }
    TypeElement superElement = superOf(baseType);
    if (superElement != null) {
      addSuperType(superElement, null);
    }
    readSubTypes();
    processCompleted();
  }

  private void readSubTypes() {
    if (hasSubTypes()) {
      for (TypeSubTypeMeta subType : subTypes.subTypes()) {
        currentSubType = subType;
        TypeElement element = context.element(subType.type());
        currentSubType.setElement(element);
        addSuperType(element, baseType);
      }
    }
  }

  List<TypeSubTypeMeta> subTypes() {
    return subTypes.subTypes();
  }

  void processCompleted() {
    constructor = determineConstructor();
    if (constructor != null) {
      List<MethodReader.MethodParam> params = constructor.getParams();
      for (MethodReader.MethodParam param : params) {
        constructorParamMap.put(param.name(), param);
      }
    }
    matchFieldsToSetterOrConstructor();
    matchFieldsToGetter();
  }

  private void addSuperType(TypeElement element, TypeElement matchType) {
    if (matchType != null && matchType == element) {
      return;
    }
    String type = element.getQualifiedName().toString();
    if (!type.equals(JAVA_LANG_OBJECT)) {
      if (!GenericType.isGeneric(type)) {
        read(element, matchType);
        addSuperType(superOf(element), matchType);
      }
    }
  }

  private TypeElement superOf(TypeElement element) {
    return (TypeElement) context.asElement(element.getSuperclass());
  }

  boolean hasSubTypes() {
    return subTypes.hasSubTypes();
  }
}
