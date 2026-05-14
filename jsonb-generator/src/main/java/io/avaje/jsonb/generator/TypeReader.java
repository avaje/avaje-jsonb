package io.avaje.jsonb.generator;

import static io.avaje.jsonb.generator.APContext.asTypeElement;
import static io.avaje.jsonb.generator.APContext.logError;
import static io.avaje.jsonb.generator.APContext.logNote;
import static io.avaje.jsonb.generator.APContext.logWarn;
import static io.avaje.jsonb.generator.APContext.typeElement;
import static io.avaje.jsonb.generator.ProcessingContext.importedJson;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

/**
 * Read points for field injection and method injection on baseType plus inherited injection points.
 */
final class TypeReader {

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";
  private static final String JAVA_LANG_THROWABLE = "java.lang.Throwable";
  private static final Set<String> THROWABLE_INCLUDES = Set.of("getMessage", "getCause", "getStackTrace", "getSuppressed");
  private static final Set<String> THROWABLE_FIELDS = Set.of("detailMessage", "suppressedExceptions", "stackTrace");

  private final List<MethodReader> publicConstructors = new ArrayList<>();
  private final List<FieldReader> allFields = new ArrayList<>();
  private final Map<String, FieldReader> allFieldMap = new HashMap<>();
  private final Map<String, MethodReader> maybeSetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> maybeGetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> allGetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> allSetterMethods = new LinkedHashMap<>();
  private final Map<String, MethodReader> unmappedSetterMethods = new LinkedHashMap<>();
  private final Map<String, String> propertySetterMethods = new LinkedHashMap<>();

  private final TypeSubTypeReader subTypes;
  private final TypeElement baseType;
  private final List<String> genericTypeParams;
  private final NamingConvention namingConvention;
  private final boolean hasJsonAnnotation;
  private final String errorContext;
  private MethodReader constructor;
  private boolean defaultPublicConstructor;
  private final Map<String, MethodReader.MethodParam> constructorParamMap = new LinkedHashMap<>();
  private TypeSubTypeMeta currentSubType;
  private boolean nonAccessibleField;

  private final Map<String, Element> mixInFields;
  private final String typePropertyKey;
  private final Map<String, Integer> frequencyMap = new HashMap<>();
  private final List<MethodProperty> methodProperties = new ArrayList<>();

  private boolean optional;

  /**
   * Set when the type is known to extend Throwable
   */
  private boolean extendsThrowable;

  private final boolean hasJsonCreator;
  private final boolean pkgPrivate;

  TypeReader(String errorContext, TypeElement baseType, TypeElement mixInType, NamingConvention namingConvention, String typePropertyKey) {
    this.errorContext = errorContext;
    this.baseType = baseType;
    this.genericTypeParams = initTypeParams(baseType);
    Optional<ExecutableElement> jsonCreator = Optional.empty();
    if (mixInType == null) {
      this.mixInFields = new HashMap<>();
    } else {
      jsonCreator =
        ElementFilter.methodsIn(mixInType.getEnclosedElements()).stream()
          .filter(CreatorPrism::isPresent)
          .findFirst();
      this.mixInFields =
        mixInType.getEnclosedElements().stream()
          .filter(e -> e.getKind() == ElementKind.FIELD)
          .collect(Collectors.toMap(e -> e.getSimpleName().toString(), e -> e));
    }
    this.namingConvention = namingConvention;
    this.hasJsonAnnotation = JsonPrism.isPresent(baseType) || importedJson(baseType).isPresent();
    this.subTypes = new TypeSubTypeReader(baseType);
    this.typePropertyKey = typePropertyKey;

    jsonCreator = jsonCreator.or(baseJsonCreator(baseType));
    constructor = jsonCreator
      .map(this::readJsonCreator)
      .orElse(null);

    this.hasJsonCreator = jsonCreator.isPresent();
    this.pkgPrivate =
      !baseType.getModifiers().contains(Modifier.PUBLIC)
        || jsonCreator.filter(e -> !e.getModifiers().contains(Modifier.PUBLIC)).isPresent();
  }

  private MethodReader readJsonCreator(ExecutableElement ex) {
    var mods = ex.getModifiers();
    if (ex.getKind() != ElementKind.CONSTRUCTOR
        && !mods.contains(Modifier.STATIC)
        && Util.isPublic(ex)) {
      logError(ex, "@Json.Creator can only be placed on constructors and static factory methods");
    }
    return new MethodReader(ex).read();
  }

  private static Supplier<Optional<? extends ExecutableElement>> baseJsonCreator(TypeElement baseType) {
    return () ->
      baseType.getEnclosedElements().stream()
        .filter(CreatorPrism::isPresent)
        .map(ExecutableElement.class::cast)
        .findFirst();
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
    read(type, null);
  }

  private void read(TypeElement type, DeclaredType superContext) {
    final List<FieldReader> localFields = new ArrayList<>();

    for (Element element : type.getEnclosedElements()) {
      switch (element.getKind()) {
        case CONSTRUCTOR:
          readConstructor(element, type);
          break;
        case FIELD:
          readField(element, localFields, superContext);
          break;
        case METHOD:
          readMethod(element, localFields);
          break;
      }
    }

    if (hasJsonCreator) {
      for (var param : constructor.getParams()) {
        var name = param.name();
        var element = param.element();
        var aliases = aliasesForParam(element);

        var matchingField =
          localFields.stream()
            .filter(field -> isMatchParam(field, name, aliases))
            .findFirst();
        matchingField.ifPresentOrElse(f -> f.readParam(element), () -> readField(element, localFields));
      }
    }

    if (currentSubType == null && type != baseType) {
      allFields.addAll(0, localFields);
      for (final FieldReader localField : localFields) {
        allFieldMap.put(localField.fieldName() + localField.adapterShortType(), localField);
      }
    } else {
      for (final FieldReader localField : localFields) {
        final FieldReader commonField = allFieldMap.get(localField.fieldName() + localField.adapterShortType());
        if (commonField == null) {
          allFields.add(localField);
          allFieldMap.put(localField.fieldName() + localField.adapterShortType(), localField);
        } else {
          commonField.addSubType(currentSubType);
          if (localField.includeFromJson() && !commonField.includeFromJson()) {
            commonField.enableDeserialize();
          }
        }
        if (commonField == null && currentSubType != null) {
          localField.setSubTypeField();
        }
      }
    }
  }

  private static boolean isMatchParam(FieldReader field, String name, Set<String> aliases) {
    return field.propertyName().equals(name)
      || field.fieldName().equals(name)
      || field.aliases().contains(name)
      || aliases.contains(field.propertyName())
      || aliases.contains(field.fieldName());
  }

  private static Set<String> aliasesForParam(VariableElement element) {
    return AliasPrism.getOptionalOn(element).map(AliasPrism::value).stream()
      .flatMap(List::stream)
      .collect(Collectors.toSet());
  }

  private void readField(Element element, List<FieldReader> localFields) {
    readField(element, localFields, null);
  }

  private void readField(Element element, List<FieldReader> localFields, DeclaredType superContext) {
    final Element mixInField = mixInFields.get(element.getSimpleName().toString());
    if (mixInField != null && APContext.types().isSameType(mixInField.asType(), element.asType())) {
      var mixinModifiers = new HashSet<>(mixInField.getModifiers());
      var modifiers = new HashSet<>(mixInField.getModifiers());

      Arrays.stream(Modifier.values())
        .filter(m -> m != Modifier.PRIVATE || m != Modifier.PROTECTED || m != Modifier.PUBLIC)
        .forEach(m -> {
          modifiers.remove(m);
          mixinModifiers.remove(m);
        });

      if (!modifiers.equals(mixinModifiers)) {
       APContext.logError(mixInField, "mixIn fields must have the same modifiers as the target class");
      }
      element = mixInField;
    }
    if (element.asType().toString().contains("java.util.Optional")) {
      optional = true;
    }
    if (includeField(element)) {
      final var frequency = frequency(element.getSimpleName().toString());
      final TypeMirror resolvedType = resolveFieldType(element, superContext);
      localFields.add(
          new FieldReader(
              element,
              resolvedType,
              namingConvention,
              currentSubType,
              genericTypeParams,
              frequency,
              hasJsonCreator));
    }
  }

  private static TypeMirror resolveFieldType(Element element, DeclaredType superContext) {
    if (superContext == null) return null;
    try {
      return APContext.types().asMemberOf(superContext, element);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static DeclaredType toDeclaredType(TypeMirror mirror) {
    if (mirror instanceof DeclaredType) {
      return (DeclaredType) mirror;
    }
    return null;
  }

  /**
   * Compute and return the frequency of the key / json property name.
   */
  private Integer frequency(String key) {
    return frequencyMap.compute(key, (k, v) -> v == null ? 0 : v + 1);
  }

  private boolean includeField(Element element) {
    if (extendsThrowable) {
      return !element.getModifiers().contains(Modifier.TRANSIENT)
        && !element.getModifiers().contains(Modifier.STATIC)
        && !THROWABLE_FIELDS.contains(element.getSimpleName().toString());
    }

    return !element.getModifiers().contains(Modifier.TRANSIENT)
      && !element.getModifiers().contains(Modifier.STATIC);
  }

  private void readConstructor(Element element, TypeElement type) {
    if (constructor != null) return;
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
    MethodReader methodReader = new MethodReader(ex).read();
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

  private void readMethod(Element element, List<FieldReader> localFields) {
    ExecutableElement methodElement = (ExecutableElement) element;
    if (checkMethod2(methodElement)) {
      List<? extends VariableElement> parameters = methodElement.getParameters();
      final String methodKey = methodElement.getSimpleName().toString();
      MethodReader methodReader = new MethodReader(methodElement).read();
      if (parameters.size() == 1) {
        var param = (VariableElement) parameters.get(0);
        if (UnmappedPrism.isPresent(methodElement) || UnmappedPrism.isPresent(param)) {
          unmappedSetterMethods.put(methodKey, methodReader);
        }
        PropertyPrism.getOptionalOn(methodElement)
          .map(PropertyPrism::value)
          .map(Util::escapeQuotes)
          .ifPresent(propName -> propertySetterMethods.put(methodKey, propName));
        maybeSetterMethods.putIfAbsent(methodKey, methodReader);
        allSetterMethods.put(methodKey.toLowerCase(), methodReader);
      } else if (parameters.isEmpty()) {
        TypeMirror returnType = methodElement.getReturnType();
        if (!"void".equals(returnType.toString())) {
          maybeGetterMethods.putIfAbsent(methodKey, methodReader);
          allGetterMethods.put(methodKey.toLowerCase(), methodReader);
        }
      }
    }
    // for getter/accessor methods only (setter case handled via propertySetterMethods)
    PropertyPrism.getOptionalOn(methodElement)
      .filter(p -> !hasRecordPropertyAnnotation(methodElement))
      .filter(p -> methodElement.getParameters().isEmpty())
      .ifPresent(propertyPrism -> {
        // getter property as simulated read-only field with getter method
        final var frequency = frequency(propertyPrism.value());
        final var reader = new FieldReader(element, namingConvention, currentSubType, genericTypeParams, frequency);
        reader.getterMethod(new MethodReader(methodElement));
        localFields.add(reader);
      });
  }

  private boolean hasRecordPropertyAnnotation(ExecutableElement methodElement) {
    try {
      return APContext.jdkVersion() >= 16
        && Optional.ofNullable(recordComponentFor(methodElement))
        .map(Element.class::cast)
        .flatMap(TypeReader::matchingField)
        .filter(PropertyPrism::isPresent)
        .isPresent();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * e is a RecordComponentElement that doesn't have the annotation
   * look up the field by name to see if the annotation is present
   */
  private static Optional<VariableElement> matchingField(Element e) {
    return ElementFilter.fieldsIn(e.getEnclosingElement().getEnclosedElements()).stream()
      .filter(f -> f.getSimpleName().contentEquals(e.getSimpleName()))
      .findAny();
  }

  private static Object recordComponentFor(ExecutableElement methodElement) throws Exception {
    return Elements.class
      .getMethod("recordComponentFor", ExecutableElement.class)
      .invoke(APContext.elements(), methodElement);
  }

  private boolean checkMethod2(ExecutableElement methodElement) {
    if (!Util.isPublic(methodElement)) {
      return false;
    }
    if (extendsThrowable) {
      return THROWABLE_INCLUDES.contains(methodElement.getSimpleName().toString());
    }
    return true;
  }

  private void matchFieldsToSetterOrConstructor() {
    for (FieldReader field : allFields) {
      if (field.includeFromJson() && !field.isConstructorParam()) {
        if (constructorParamMap.get(field.fieldName()) != null) {
          field.setConstructorParam();
        } else {
          matchFieldToSetter(field);
        }
      }
    }
  }

  private void matchFieldToSetter(FieldReader field) {
    if (hasNoSetter(field)) {
      if (isCollectionType(field.type())) {
        field.setUseGetterAddAll();
      } else if (hasSubTypes() && isSubTypeConstructorParam(field)) {
        field.setConstructorParam();
      } else if (ProcessingContext.isCascadeType(baseType)) {
        nonAccessibleField = true;
        var module = APContext.getProjectModuleElement();
        if (!module.isUnnamed() && module.equals(APContext.elements().getModuleOf(baseType))) {
          logWarn(
              field.element(),
              "%s%s, non public field %s with no matching setter or constructor?",
              errorContext,
              baseType,
              field.fieldName());
        }
      } else {
        logError(field.element(), errorContext + baseType + ", non public field %s with no matching setter or constructor?", field.fieldName());
      }
    }
  }

  private boolean isSubTypeConstructorParam(FieldReader field) {
    final String name = field.fieldName();
    final String propName = field.propertyName();
    return subTypes.subTypes().stream()
        .anyMatch(st -> st.hasConstructorParam(name) || st.hasConstructorParam(propName));
  }

  private boolean isCollectionType(GenericType genericType) {
    final String topType = genericType.topType();
    return "java.util.List".equals(topType)
      || "java.util.Set".equals(topType)
      || "java.util.Collection".equals(topType);
  }

  private boolean hasNoSetter(FieldReader field) {
    var propName = field.propertyName();
    var fieldName = field.fieldName();
    return !matchSetter(fieldName, field, false)
        && !matchSetter(fieldName, field, true)
        && !matchFieldToSetterByParam(fieldName, field)
        && !matchSetter(propName, field, false)
        && !matchSetter(propName, field, true)
        && !matchFieldToSetterByParam(propName, field)
        && !field.isPublicField()
        && !field.isSubTypeField();
  }

  private boolean matchFieldToSetterByParam(String fieldName, FieldReader field) {
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

  private boolean matchSetter(String name, FieldReader field, boolean loose) {
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
    }
    return maybeSetterMethods.get(name);
  }

  private void upgradeFieldsWithUnmappedSetters() {
    if (unmappedSetterMethods.isEmpty()) return;
    for (FieldReader field : allFields) {
      if (!field.isUnmapped() && field.setter() != null) {
        var matched = unmappedSetterMethods.remove(field.setter().getName());
        if (matched != null) {
          field.setAsUnmapped();
        }
      }
    }
  }

  private void upgradeFieldsWithPropertySetters() {
    if (propertySetterMethods.isEmpty()) return;
    for (FieldReader field : allFields) {
      if (field.setter() != null) {
        var propName = propertySetterMethods.remove(field.setter().getName());
        if (propName != null) {
          field.overridePropertyName(propName);
        }
      }
    }
  }

  private void createOrphanUnmappedSetterFields() {
    for (MethodReader setter : unmappedSetterMethods.values()) {
      var setterElement = setter.element();
      var param = (VariableElement) setterElement.getParameters().get(0);
      final var frequency = frequency(param.getSimpleName().toString());
      FieldReader unmappedReader =
          new FieldReader(
              param,
              null,
              namingConvention,
              currentSubType,
              genericTypeParams,
              frequency,
              hasJsonCreator);
      unmappedReader.setAsUnmapped();
      unmappedReader.setOrphanUnmappedSetter();
      unmappedReader.setterMethod(setter);
      allFields.add(unmappedReader);
      allFieldMap.put(
          unmappedReader.fieldName() + unmappedReader.adapterShortType(), unmappedReader);
    }
    unmappedSetterMethods.clear();
    // Orphan property-setter: setter with @Json.Property but no matching backing field
    final var existingFieldNames = hasJsonCreator
        ? allFields.stream().map(FieldReader::fieldName).collect(toSet())
        : Set.of();
    for (Map.Entry<String, String> entry : propertySetterMethods.entrySet()) {
      var setter = maybeSetterMethods.get(entry.getKey());
      if (setter == null) continue;
      var setterElement = setter.element();
      var param = (VariableElement) setterElement.getParameters().get(0);
      final var paramName = param.getSimpleName().toString();
      if (existingFieldNames.contains(paramName)) {
        logError(setterElement,
            "@Json.Property setter parameter name '%s' conflicts with an existing field or constructor parameter name. Rename the setter parameter to avoid a variable name collision in generated code.",
            paramName);
        continue;
      }
      final var frequency = frequency(entry.getValue());
      FieldReader orphanReader =
          new FieldReader(
              param,
              null,
              namingConvention,
              currentSubType,
              genericTypeParams,
              frequency,
              hasJsonCreator);
      orphanReader.overridePropertyName(entry.getValue());
      orphanReader.setterMethod(setter);
      allFields.add(orphanReader);
      allFieldMap.put(orphanReader.fieldName() + orphanReader.adapterShortType(), orphanReader);
    }
    propertySetterMethods.clear();
  }

  private void matchFieldsToGetter() {
    for (FieldReader field : allFields) {
      if (field.includeToJson()) {
        matchFieldToGetter(field);
      } else if (field.isOrphanUnmappedSetter()) {
        // Orphan unmapped setter (no backing field): try to find a getter for round-trip serialization
        tryMatchUnmappedGetter(field);
      }
    }
  }

  private void tryMatchUnmappedGetter(FieldReader field) {
    var fieldName = field.fieldName();
    var propName = field.propertyName();
    if (matchGetter(fieldName, field, false)
        || matchGetter(fieldName, field, true)
        || matchGetter(propName, field, false)
        || matchGetter(propName, field, true)) {
      field.enableSerialize();
    }
  }

  private void matchFieldToGetter(FieldReader field) {
    var propName = field.propertyName();
    var fieldName = field.fieldName();
    if (!matchGetter(fieldName, field, false)
        && !matchGetter(fieldName, field, true)
        && !matchGetter(propName, field, false)
        && !matchGetter(propName, field, true)
        && !field.isPublicField()
        && !field.isSubTypeField()) {
      nonAccessibleField = true;
      if (hasJsonAnnotation) {
        logError(errorContext + baseType + ", non accessible field " + field.fieldName() + " with no matching getter?");
      } else {
        logNote(errorContext + baseType + ", non accessible field " + field.fieldName());
      }
    }
  }

  private boolean matchGetter(String name, FieldReader field, boolean loose) {
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
    }
  return allGetterMethods.get(name.toLowerCase());
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
    if (constructor != null) {
      return constructor;
    }
    if (defaultPublicConstructor && !allSetterMethods.isEmpty()) {
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

    // find the right constructor
    var constructorFields = allFields.stream()
      .filter(FieldReader::includeFromJson)
      .filter(this::hasNoSetter)
      .map(f -> f.element().asType().toString())
      .map(Util::trimAnnotations)
      .collect(toSet());

    return allPublic.stream()
      .filter(c -> c.getParams().size() == constructorFields.size())
      .filter(c -> c.getParams().stream()
        .map(p -> p.element().asType().toString())
        .map(Util::trimAnnotations)
        .allMatch(constructorFields::contains))
      .findFirst()
      .orElseGet(this::largest);
  }

  private MethodReader largest() {
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
      addSuperType(superElement, toDeclaredType(baseType.getSuperclass()));
    }
    readSubTypes();
    processCompleted();
  }

  private void readSubTypes() {
    if (hasSubTypes()) {
      for (TypeSubTypeMeta subType : subTypes.subTypes()) {
        currentSubType = subType;
        TypeElement element = typeElement(subType.type());
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

      var constructorParams = constructor.getParams();
      for (var param : constructorParams) {
        var name = param.name();
        var matchingField =
            allFields.stream()
                .filter(f -> !f.isConstructorParam())
                .filter(f -> f.propertyName().equals(name) || f.fieldName().equals(name))
                .findFirst();
        matchingField.ifPresent(FieldReader::setConstructorParam);
      }

      List<MethodReader.MethodParam> params = constructorParams;
      for (MethodReader.MethodParam param : params) {
        constructorParamMap.put(param.name(), param);
      }
    }
    matchFieldsToSetterOrConstructor();
    upgradeFieldsWithUnmappedSetters();
    upgradeFieldsWithPropertySetters();
    createOrphanUnmappedSetterFields();
    matchFieldsToGetter();
    if (extendsThrowable || allFields.isEmpty() && subTypes.subTypes().isEmpty()) {
      initReadOnlyMethods();
    }
  }

  /** Set the index position of the fields (for PropertyNames). */
  private void setFieldPositions() {
    final int offset = subTypes.hasSubTypes() ? 1 : 0;
    // skip position if property == a type property
    final var fields = allFields.stream()
      .filter(f -> !f.propertyName().equals(typePropertyKey))
      .collect(Collectors.toList());

    final Map<String, Integer> seenPositions = new LinkedHashMap<>();
    int nextPos = 0;
    for (final var field : fields) {
      final String propName = field.propertyName();
      final Integer existingPos = seenPositions.get(propName);
      if (existingPos != null) {
        field.position(existingPos);
      } else {
        final int pos = nextPos + offset;
        field.position(pos);
        seenPositions.put(propName, pos);
        nextPos++;
      }
    }
  }

  private void addSuperType(TypeElement element) {
    addSuperType(element, null);
  }

  private void addSuperType(TypeElement element, DeclaredType concreteType) {
    String type = element.getQualifiedName().toString();
    if (!JAVA_LANG_OBJECT.equals(type) && !GenericType.isGeneric(type)) {
      if (JAVA_LANG_THROWABLE.equals(type)) {
        extendsThrowable = true;
      }
      read(element, concreteType);
      TypeElement nextSuper = superOf(element);
      if (nextSuper != null) {
        addSuperType(nextSuper, toDeclaredType(element.getSuperclass()));
      }
    }
  }

  private TypeElement superOf(TypeElement element) {
    return asTypeElement(element.getSuperclass());
  }

  boolean hasSubTypes() {
    return subTypes.hasSubTypes();
  }

  public boolean hasOptional() {
    return optional;
  }

  public List<MethodProperty> methodProperties() {
    return methodProperties;
  }

  private void initReadOnlyMethods() {
    int pos = 0;
    for (MethodReader methodReader : maybeGetterMethods.values()) {
      var property = new FieldProperty(methodReader);
      property.setGetterMethod(methodReader);
      property.setPosition(pos);
      pos++;
      String name = initPropertyName(methodReader.getName(), property);
      String propertyName = namingConvention.from(name);
      methodProperties.add(new MethodProperty(propertyName, property));
    }
  }

  private String initPropertyName(String name, FieldProperty property) {
    if (property.typeBooleanWithIsPrefix()) {
      return Util.initLower(name.substring(2));
    }
    if (name.length() > 3 && name.startsWith("get") && Character.isUpperCase(name.charAt(3))) {
      return Util.initLower(name.substring(3));
    }
    return name;
  }

  boolean extendsThrowable() {
    return extendsThrowable;
  }

  boolean isPkgPrivate() {
    return pkgPrivate;
  }
}
