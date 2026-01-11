package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.lang.invoke.MethodHandles;
import java.util.function.Predicate;

final class PropertyIgnoreReader {

  private boolean ignoreSerialize;
  private boolean ignoreDeserialize;
  private static Predicate<ExecutableElement> isNotRecordAccessor;

  PropertyIgnoreReader(Element element, String propertyName) {
    final var enclosingElements = element.getEnclosingElement().getEnclosedElements();

    var isField = element instanceof VariableElement;
    boolean propertyMethodOverride =
      isField
        && ElementFilter.methodsIn(enclosingElements).stream()
        .filter(PropertyPrism::isPresent)
        .filter(isNotRecordAccessor)
        .map(PropertyPrism::getInstanceOn)
        .map(PropertyPrism::value)
        .anyMatch(propertyName::equals);

    ignoreSerialize = propertyMethodOverride;

    if (isField
        && element.getSimpleName().toString().startsWith("_$")
        && element.getEnclosingElement().getAnnotationMirrors().stream()
            .anyMatch(a -> "Entity".equals(UType.parse(a.getAnnotationType()).shortType()))) {
      ignoreSerialize = true;
      ignoreDeserialize = true;
    }

    final IgnorePrism ignored = IgnorePrism.getInstanceOn(element);
    if (ignored != null) {
      ignoreDeserialize = !ignored.deserialize();
      ignoreSerialize = propertyMethodOverride || !ignored.serialize();
    }
  }

  boolean serialize() {
    return !ignoreSerialize;
  }

  boolean deserialize() {
    return !ignoreDeserialize;
  }

  // needed to filter out record components from @Json.Property Overriding
  static {
    try {
      var recordComponentFor =
        MethodHandles.lookup()
          .unreflect(Elements.class.getMethod("recordComponentFor", ExecutableElement.class));
      isNotRecordAccessor = e -> {
        try {
          return recordComponentFor.invoke(APContext.elements(), e) == null;
        } catch (Throwable e1) {
          return true;
        }
      };
    } catch (Exception ex) {
      isNotRecordAccessor = e -> true;
    }
  }
}
