package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.ProcessingContext.*;

import javax.lang.model.element.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Read points for field injection and method injection
 * on baseType plus inherited injection points.
 */
final class TypeReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";

  private final List<MethodReader> publicConstructors = new ArrayList<>();
  private final List<FieldReader> allFields = new ArrayList<>();
  private final Map<String, FieldReader> allFieldMap = new HashMap<>();
  private final Map<String, MethodReader> maybeSetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> maybeGetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> allGetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> allSetterMethods = new LinkedHashMap<>();

  private final TypeSubTypeReader subTypes;
  private final TypeElement baseType;
  private final List<String> genericTypeParams;
  private final NamingConvention namingConvention;
  private final boolean hasJsonAnnotation;
  private MethodReader constructor;
  private boolean defaultPublicConstructor;
  private final Map<String, MethodReader.MethodParam> constructorParamMap = new LinkedHashMap<>();
  private TypeSubTypeMeta currentSubType;
  private boolean nonAccessibleField;

  private final Map<String, Element> mixInFields;

  private final String typePropertyKey;

  TypeReader(TypeElement baseType, TypeElement mixInType, NamingConvention namingConvention, String typePropertyKey) {
    this.baseType = baseType;
    this.genericTypeParams = initTypeParams(baseType);
    if (mixInType == null) {
      this.mixInFields = new HashMap<>();
    } else {
      this.mixInFields =
        mixInType.getEnclosedElements().stream()
          .filter(e -> e.getKind() == ElementKind.FIELD)
          .collect(Collectors.toMap(e -> e.getSimpleName().toString(), e -> e));
    }
    this.namingConvention = namingConvention;
    this.hasJsonAnnotation = JsonPrism.isPresent(baseType);
    this.subTypes = new TypeSubTypeReader(baseType);
    this.typePropertyKey = typePropertyKey;
  }

  private List<String> initTypeParams(TypeElement beanType) {
    if (beanType.getTypeParameters().isEmpty()) {
      return Collections.emptyList();
    }
    return beanType.getTypeParameters()
      .stream()
      .map(Object::toString)
      .collect(Collectors.toList());
  }

  int genericTypeParamsCount() {
    return genericTypeParams.size();
  }

  void read(TypeElement type) {
    final List<FieldReader> localFields = new ArrayList<>();
    for (Element element : type.getEnclosedElements()) {
      switch (element.getKind()) {
        case CONSTRUCTOR:
          readConstructor(element, type);
          break;
        case FIELD:
          readField(element, localFields);
          break;
        case METHOD:
          readMethod(element, type);
          break;
      }
    }
    if (currentSubType == null && type != baseType) {
      allFields.addAll(0, localFields);
      for (FieldReader localField : localFields) {
        allFieldMap.put(localField.fieldName(), localField);
      }
    } else {
      for (FieldReader localField : localFields) {
        FieldReader commonField = allFieldMap.get(localField.fieldName());
        if (commonField == null) {
          allFields.add(localField);
          allFieldMap.put(localField.fieldName(), localField);
        } else {
          commonField.addSubType(currentSubType);
        }

        if (commonField == null && currentSubType != null) {
          localField.isSubTypeField(true);
        }
      }
    }
  }

  private void readField(Element element, List<FieldReader> localFields) {
    final Element mixInField = mixInFields.get(element.getSimpleName().toString());
    if (mixInField != null && mixInField.asType().equals(element.asType())) {
      element = mixInField;
    }
    if (includeField(element)) {
      localFields.add(new FieldReader(element, namingConvention, currentSubType, genericTypeParams));
    }
  }

  private boolean includeField(Element element) {
    return !element.getModifiers().contains(Modifier.TRANSIENT)
      && !element.getModifiers().contains(Modifier.STATIC);
  }

  private void readConstructor(Element element, TypeElement type) {
    if (currentSubType != null) {
      if (currentSubType.element() != type) {
        // logError("subType " + currentSubType.element() + " ignore constructor " + element);
        return;
      }
    } else if (type != baseType) {
      // logError("baseType " + baseType + " ignore constructor: " + element);
      return;
    }
    ExecutableElement ex = (ExecutableElement) element;
    MethodReader methodReader = new MethodReader(ex, baseType).read();
    if (methodReader.isPublic() || hasSubTypes() && methodReader.isProtected()) {
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
      MethodReader methodReader = new MethodReader(methodElement, type).read();
      if (parameters.size() == 1) {
        if (!maybeSetterMethods.containsKey(methodKey)) {
          maybeSetterMethods.put(methodKey, methodReader);
        }
        allSetterMethods.put(methodKey.toLowerCase(), methodReader);
      } else if (parameters.size() == 0) {
        if (!maybeGetterMethods.containsKey(methodKey)) {
          maybeGetterMethods.put(methodKey, methodReader);
        }
        allGetterMethods.put(methodKey.toLowerCase(), methodReader);
      }
    }
  }

  private void matchFieldsToSetterOrConstructor() {
    for (FieldReader field : allFields) {
      if (field.includeFromJson()) {
        if (constructorParamMap.get(field.fieldName()) != null) {
          field.constructorParam();
        } else {
          matchFieldToSetter(field);
        }
      }
    }
  }

  private void matchFieldToSetter(FieldReader field) {
    if (!matchFieldToSetter2(field, false)
      && !matchFieldToSetter2(field, true)
      && !matchFieldToSetterByParam(field)
      && !field.isPublicField()
      && !field.isSubTypeField()) {
      logError("Non public field " + baseType + " " + field.fieldName() + " with no matching setter or constructor?");
    }
  }

  private boolean matchFieldToSetterByParam(FieldReader field) {
    String fieldName = field.fieldName();
    for (MethodReader methodReader : maybeSetterMethods.values()) {
      List<MethodReader.MethodParam> params = methodReader.getParams();
      MethodReader.MethodParam methodParam = params.get(0);
      if (methodParam.name().equals(fieldName)) {
        field.setterMethod(methodReader);
        return true;
      }
    }
    return false;
  }

  private boolean matchFieldToSetter2(FieldReader field, boolean loose) {
    String name = field.fieldName();
    MethodReader setter = setterLookup(name, loose);
    if (setter != null) {
      field.setterMethod(setter);
      return true;
    }
    setter = setterLookup(setterName(name), loose);
    if (setter != null) {
      field.setterMethod(setter);
      return true;
    }
    if (field.typeBooleanWithIsPrefix()) { // isActive -> setActive() for boolean and Boolean
      setter = setterLookup(setterName(name.substring(2)), loose);
      if (setter != null) {
        field.setterMethod(setter);
        return true;
      }
    }
    return false;
  }

  private MethodReader setterLookup(String name, boolean loose) {
    if (loose) {
      return allSetterMethods.get(name.toLowerCase());
    } else {
      return maybeSetterMethods.get(name);
    }
  }

  private void matchFieldsToGetter() {
    for (FieldReader field : allFields) {
      if (field.includeToJson()) {
        matchFieldToGetter(field);
      }
    }
  }

  private void matchFieldToGetter(FieldReader field) {
    if (!matchFieldToGetter2(field, false)
      && !matchFieldToGetter2(field, true)
      && !field.isPublicField()
      && !field.isSubTypeField()) {
      nonAccessibleField = true;
      if (hasJsonAnnotation) {
        logError("Non accessible field " + baseType + " " + field.fieldName() + " with no matching getter?");
      } else {
        logDebug("Non accessible field " + baseType + " " + field.fieldName());
      }
    }
  }

  private boolean matchFieldToGetter2(FieldReader field, boolean loose) {
    String name = field.fieldName();
    MethodReader getter = getterLookup(name, loose);
    if (getter != null) {
      field.getterMethod(getter);
      return true;
    }
    getter = getterLookup(getterName(name), loose);
    if (getter != null) {
      field.getterMethod(getter);
      return true;
    }
    getter = getterLookup(isGetterName(name), loose);
    if (getter != null) {
      field.getterMethod(getter);
      return true;
    }
    if (field.typeObjectBooleanWithIsPrefix()) { // isRegistered -> getRegistered() for Boolean
      getter = getterLookup(getterName(name.substring(2)), loose);
      if (getter != null) {
        field.getterMethod(getter);
        return true;
      }
    }
    return false;
  }

  private MethodReader getterLookup(String name, boolean loose) {
    if (!loose) {
      return maybeGetterMethods.get(name);
    } else {
      return allGetterMethods.get(name.toLowerCase());
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

  boolean nonAccessibleField() {
    return nonAccessibleField;
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
    // find the largest constructor
    int argCount = 0;
    MethodReader largestConstructor = null;
    for (MethodReader ctor : publicConstructors) {
      if (ctor.isPublic()) {
        int paramCount = ctor.getParams().size();
        if (paramCount > argCount) {
          largestConstructor = ctor;
          argCount = paramCount;
        }
      }
    }
    return largestConstructor;
  }

  void process() {
    String base = baseType.getQualifiedName().toString();
    if (!GenericType.isGeneric(base)) {
      read(baseType);
    }
    TypeElement superElement = superOf(baseType);
    if (superElement != null) {
      addSuperType(superElement);
    }
    readSubTypes();
    processCompleted();
  }

  private void readSubTypes() {
    if (hasSubTypes()) {
      for (TypeSubTypeMeta subType : subTypes.subTypes()) {
        currentSubType = subType;
        TypeElement element = element(subType.type());
        currentSubType.setElement(element);
        addSuperType(element);
      }
    }
  }

  List<TypeSubTypeMeta> subTypes() {
    return subTypes.subTypes();
  }

  void processCompleted() {
    setFieldPositions();
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

  /**
   * Set the index position of the fields (for PropertyNames).
   */
  private void setFieldPositions() {
    final int offset = subTypes.hasSubTypes() ? 1 : 0;
    //skip position if property == a type property
    final var fields = allFields.stream()
      .filter(f -> !f.propertyName().equals(typePropertyKey))
      .collect(Collectors.toList());

    for (int pos = 0, size = fields.size(); pos < size; pos++) {
      final var field = fields.get(pos);
      field.position(pos + offset);
    }
  }

  private void addSuperType(TypeElement element) {
    String type = element.getQualifiedName().toString();
    if (!JAVA_LANG_OBJECT.equals(type) && !GenericType.isGeneric(type)) {
      read(element);
      addSuperType(superOf(element));
    }
  }

  private TypeElement superOf(TypeElement element) {
    return (TypeElement) asElement(element.getSuperclass());
  }

  boolean hasSubTypes() {
    return subTypes.hasSubTypes();
  }
}
