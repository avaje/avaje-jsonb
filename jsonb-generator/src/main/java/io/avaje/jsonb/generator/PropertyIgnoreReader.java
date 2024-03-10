package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

final class PropertyIgnoreReader {

  private boolean ignoreSerialize;
  private boolean ignoreDeserialize;
  private static Function<Iterable<? extends Element>, Set<ExecutableElement>> getRecordMethods;

  PropertyIgnoreReader(Element element, String propertyName) {
    final var enclosingElements = element.getEnclosingElement().getEnclosedElements();

    var recordMethods = getRecordMethods.apply(enclosingElements);
    boolean propertyMethodOverride =
      element instanceof VariableElement
        && ElementFilter.methodsIn(enclosingElements).stream()
        .filter(PropertyPrism::isPresent)
        .filter(not(recordMethods::contains))
        .map(PropertyPrism::getInstanceOn)
        .map(PropertyPrism::value)
        .anyMatch(propertyName::equals);

    ignoreSerialize = propertyMethodOverride;

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
      var lookup = MethodHandles.lookup();
      var recordComponentsMethod =
        lookup.findStatic(
          ElementFilter.class,
          "recordComponentsIn",
          MethodType.methodType(List.class, Iterable.class));

      Function<Iterable<? extends Element>, List<Object>> recordComponentsIn =
        e -> {
          try {
            return (List<Object>) recordComponentsMethod.invoke(e);
          } catch (Throwable e1) {
            throw new IllegalStateException();
          }
        };

      var getAccessor =
        lookup.findVirtual(
          Class.forName("javax.lang.model.element.RecordComponentElement"),
          "getAccessor",
          MethodType.methodType(ExecutableElement.class));

      getRecordMethods = recordComponentsIn.andThen(l -> l.stream()
        .map(e -> {
          try {
            return getAccessor.invoke(e);
          } catch (Throwable e1) {
            throw new IllegalStateException();
          }
        })
        .map(ExecutableElement.class::cast)
        .collect(toSet()));
    } catch (Exception ex) {
      getRecordMethods = e -> Set.of();
    }
  }
}
