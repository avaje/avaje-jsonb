package io.avaje.jsonb.generator;

import java.util.Optional;

import javax.lang.model.element.Element;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.avaje.jsonb.Json.Property;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(
    value = Property.class,
    name = "AvajePropertyPrism",
    superInterfaces = PropertyPrism.class)
@GeneratePrism(
    value = JsonProperty.class,
    name = "JacksonPropertyPrism",
    superInterfaces = PropertyPrism.class)
public interface PropertyPrism {

  static boolean isPresent(Element element) {

    return AvajePropertyPrism.isPresent(element) || JacksonPropertyPrism.isPresent(element);
  }

  static PropertyPrism getInstanceOn(Element element) {
    return getOptionalOn(element).orElse(null);
  }

  static Optional<PropertyPrism> getOptionalOn(Element element) {
    return Optional.<PropertyPrism>empty()
        .or(() -> AvajePropertyPrism.getOptionalOn(element))
        .or(() -> JacksonPropertyPrism.getOptionalOn(element));
  }

  String value();
}
