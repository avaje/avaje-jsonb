package io.avaje.jsonb.generator;

import java.util.Optional;

import javax.lang.model.element.Element;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.google.gson.annotations.Expose;
import io.avaje.jsonb.Json.Ignore;
import io.avaje.prism.GeneratePrism;
import jakarta.json.bind.annotation.JsonbTransient;

@GeneratePrism(
  value = Ignore.class,
  name = "AvajeIgnorePrism",
  superInterfaces = IgnorePrism.class)
@GeneratePrism(
  value = JsonIgnore.class,
  name = "JacksonIgnorePrism",
  superInterfaces = IgnorePrism.class)
@GeneratePrism(
  value = Expose.class,
  name = "GsonIgnorePrism",
  superInterfaces = IgnorePrism.class)
@GeneratePrism(
  value = JsonbTransient.class,
  name = "JakartaIgnorePrism",
  superInterfaces = IgnorePrism.class)
public interface IgnorePrism {

  static IgnorePrism getInstanceOn(Element element) {
    return getOptionalOn(element).orElse(null);
  }

  static Optional<IgnorePrism> getOptionalOn(Element element) {
    return Optional.<IgnorePrism>empty()
        .or(() -> AvajeIgnorePrism.getOptionalOn(element))
        .or(() -> JacksonIgnorePrism.getOptionalOn(element))
        .or(() -> GsonIgnorePrism.getOptionalOn(element))
        .or(() -> JakartaIgnorePrism.getOptionalOn(element));
  }

  default Boolean serialize() {
    return false;
  }

  default Boolean deserialize() {
    return false;
  }
}
