package io.avaje.jsonb.generator;

import com.fasterxml.jackson.annotation.JsonValue;
import io.avaje.jsonb.Json;
import io.avaje.prism.GeneratePrism;

import javax.lang.model.element.Element;

@GeneratePrism(
    value = Json.Value.class,
    name = "AvajeValuePrism",
    superInterfaces = ValuePrism.class)
@GeneratePrism(
    value = JsonValue.class,
    name = "JacksonValuePrism",
    superInterfaces = ValuePrism.class)
public interface ValuePrism {

  static boolean isPresent(Element element) {
    return AvajeValuePrism.isPresent(element) || JacksonValuePrism.isPresent(element);
  }
}
