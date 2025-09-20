package io.avaje.jsonb.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.avaje.jsonb.Json.Property;
import io.avaje.prism.GeneratePrism;
import jakarta.json.bind.annotation.JsonbProperty;

import javax.lang.model.element.Element;
import java.util.Optional;

@GeneratePrism(
  value = Property.class,
  name = "AvajePropertyPrism",
  superInterfaces = PropertyPrism.class)
@GeneratePrism(
  value = JsonProperty.class,
  name = "JacksonPropertyPrism",
  superInterfaces = PropertyPrism.class)
@GeneratePrism(
  value = SerializedName.class,
  name = "GsonPropertyPrism",
  superInterfaces = PropertyPrism.class)
@GeneratePrism(
  value = JsonbProperty.class,
  name = "JakartaPropertyPrism",
  superInterfaces = PropertyPrism.class)
public interface PropertyPrism {

  static boolean isPresent(Element element) {
    return AvajePropertyPrism.isPresent(element)
      || (JacksonPropertyPrism.isPresent(element) && JacksonPropertyPrism.getOptionalOn(element)
            .map(JacksonPropertyPrism::value)
            .map(String::isEmpty)
            .isEmpty())
      || GsonPropertyPrism.isPresent(element)
      || (JakartaPropertyPrism.isPresent(element) && JakartaPropertyPrism.getOptionalOn(element)
            .map(JakartaPropertyPrism::value)
            .map(String::isEmpty)
            .isEmpty());
  }

  static PropertyPrism getInstanceOn(Element element) {
    return getOptionalOn(element).orElse(null);
  }

  static Optional<PropertyPrism> getOptionalOn(Element element) {
    return Optional.<PropertyPrism>empty()
        .or(() -> AvajePropertyPrism.getOptionalOn(element))
        .or(() -> JacksonPropertyPrism.getOptionalOn(element))
        .or(() -> GsonPropertyPrism.getOptionalOn(element))
        .or(() -> JakartaPropertyPrism.getOptionalOn(element));
  }

  String value();
}
