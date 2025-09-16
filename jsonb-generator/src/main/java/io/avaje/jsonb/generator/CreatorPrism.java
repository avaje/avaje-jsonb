package io.avaje.jsonb.generator;

import java.util.Optional;

import javax.lang.model.element.Element;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.avaje.jsonb.Json.Creator;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(
    value = Creator.class,
    name = "AvajeCreatorPrism",
    superInterfaces = CreatorPrism.class)
@GeneratePrism(
    value = JsonCreator.class,
    name = "JacksonCreatorPrism",
    superInterfaces = CreatorPrism.class)
public interface CreatorPrism {

  static boolean isPresent(Element element) {
    return AvajeCreatorPrism.isPresent(element) || JacksonCreatorPrism.isPresent(element);
  }

  static CreatorPrism getInstanceOn(Element element) {
    return getOptionalOn(element).orElse(null);
  }

  static Optional<CreatorPrism> getOptionalOn(Element element) {
    return Optional.<CreatorPrism>empty()
        .or(() -> AvajeCreatorPrism.getOptionalOn(element))
        .or(() -> JacksonCreatorPrism.getOptionalOn(element));
  }
}
