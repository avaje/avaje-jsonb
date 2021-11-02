package io.avaje.inject.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * Read points for field injection and method injection
 * on baseType plus inherited injection points.
 */
class TypeExtendsInjection {

  private final List<MethodReader> otherConstructors = new ArrayList<>();
  private final List<MethodReader> factoryMethods = new ArrayList<>();
  private final List<FieldReader> injectFields = new ArrayList<>();
  private final Map<String, MethodReader> injectMethods = new LinkedHashMap<>();
  private final Set<String> notInjectMethods = new HashSet<>();

  private final TypeElement baseType;
  private final ProcessingContext context;

  TypeExtendsInjection(TypeElement baseType, ProcessingContext context) {
    this.baseType = baseType;
    this.context = context;
  }

  void read(TypeElement type) {
    for (Element element : type.getEnclosedElements()) {
      switch (element.getKind()) {
        case CONSTRUCTOR:
          readConstructor(element, type);
          break;
        case FIELD:
          readField(element);
          break;
        case METHOD:
          readMethod(element, type);
          break;
      }
    }
  }

  private void readField(Element element) {
//    Inject inject = element.getAnnotation(Inject.class);
//    if (inject != null) {
//      injectFields.add(new FieldReader(element));
//    }
  }

  private void readConstructor(Element element, TypeElement type) {
    if (type != baseType) {
      // only interested in the top level constructors
      return;
    }

    ExecutableElement ex = (ExecutableElement) element;
    MethodReader methodReader = new MethodReader(context, ex, baseType).read();
//    Inject inject = element.getAnnotation(Inject.class);
//    if (inject != null) {
//      injectConstructor = methodReader;
//    } else {
      if (methodReader.isNotPrivate()) {
        otherConstructors.add(methodReader);
      }
//    }
  }

  private void readMethod(Element element, TypeElement type) {
    ExecutableElement methodElement = (ExecutableElement) element;

    final String methodKey = methodElement.getSimpleName().toString();
    if (!notInjectMethods.contains(methodKey)) {
      if (!injectMethods.containsKey(methodKey)) {
        MethodReader methodReader = new MethodReader(context, methodElement, type).read();
        if (methodReader.isNotPrivate()) {
          injectMethods.put(methodKey, methodReader);
        }
      }
    } else {
      notInjectMethods.add(methodKey);
    }
  }

  List<FieldReader> getInjectFields() {
    List<FieldReader> list = new ArrayList(injectFields);
    Collections.reverse(list);
    return list;
  }

  List<MethodReader> getInjectMethods() {
    List<MethodReader> list = new ArrayList<>(injectMethods.values());
    Collections.reverse(list);
    return list;
  }

  List<MethodReader> getFactoryMethods() {
    return factoryMethods;
  }


  MethodReader getConstructor() {
    if (otherConstructors.size() == 1) {
      return otherConstructors.get(0);
    }
    // check if there is only one public constructor
    List<MethodReader> allPublic = new ArrayList<>();
    for (MethodReader ctor : otherConstructors) {
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
