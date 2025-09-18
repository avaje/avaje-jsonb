package io.avaje.jsonb.generator;

import java.util.Optional;

import javax.lang.model.element.Element;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.avaje.jsonb.Json.Ignore;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(value = Ignore.class, name = "AvajeIgnorePrism", superInterfaces = IgnorePrism.class)
@GeneratePrism(
    value = JsonIgnore.class,
    name = "JacksonIgnorePrism",
    superInterfaces = IgnorePrism.class)
public interface IgnorePrism {

  static IgnorePrism getInstanceOn(Element element) {
    return getOptionalOn(element).orElse(null);
  }

  static Optional<IgnorePrism> getOptionalOn(Element element) {
    return Optional.<IgnorePrism>empty()
        .or(() -> AvajeIgnorePrism.getOptionalOn(element))
        .or(() -> JacksonIgnorePrism.getOptionalOn(element));
  }

  default Boolean serialize() {
    return false;
  }

  default Boolean deserialize() {
    return false;
  }
}
