package io.avaje.jsonb.generator;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Element;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.avaje.jsonb.Json.Alias;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(value = Alias.class, name = "AvajeAliasPrism", superInterfaces = AliasPrism.class)
@GeneratePrism(
    value = JsonAlias.class,
    name = "JacksonAliasPrism",
    superInterfaces = AliasPrism.class)
public interface AliasPrism {


  static Optional<AliasPrism> getOptionalOn(Element element) {
    return Optional.<AliasPrism>empty()
        .or(() -> AvajeAliasPrism.getOptionalOn(element))
        .or(() -> JacksonAliasPrism.getOptionalOn(element));
  }

  List<String> value();
}
