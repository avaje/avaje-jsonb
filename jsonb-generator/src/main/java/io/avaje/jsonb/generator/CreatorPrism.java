package io.avaje.jsonb.generator;

import javax.lang.model.element.Element;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.avaje.jsonb.Json.Creator;
import io.avaje.prism.GeneratePrism;
import jakarta.json.bind.annotation.JsonbCreator;

@GeneratePrism(
    value = Creator.class,
    name = "AvajeCreatorPrism",
    superInterfaces = CreatorPrism.class)
@GeneratePrism(
    value = JsonCreator.class,
    name = "JacksonCreatorPrism",
    superInterfaces = CreatorPrism.class)
@GeneratePrism(
    value = JsonbCreator.class,
    name = "JakartaCreatorPrism",
    superInterfaces = CreatorPrism.class)
public interface CreatorPrism {

  static boolean isPresent(Element element) {
    return AvajeCreatorPrism.isPresent(element)
      || JacksonCreatorPrism.isPresent(element)
      || JakartaCreatorPrism.isPresent(element);
  }
}
