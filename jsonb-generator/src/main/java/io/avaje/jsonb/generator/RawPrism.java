package io.avaje.jsonb.generator;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.avaje.jsonb.Json;
import io.avaje.prism.GeneratePrism;

import javax.lang.model.element.Element;

@GeneratePrism(
    value = Json.Raw.class,
    name = "AvajeRawPrism",
    superInterfaces = RawPrism.class)
@GeneratePrism(
    value = JsonRawValue.class,
    name = "JacksonRawPrism",
    superInterfaces = RawPrism.class)
public interface RawPrism {

  static boolean isPresent(Element element) {
    return AvajeRawPrism.isPresent(element) || JacksonRawPrism.isPresent(element);
  }
}
