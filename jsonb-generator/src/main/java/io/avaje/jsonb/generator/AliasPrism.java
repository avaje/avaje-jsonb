package io.avaje.jsonb.generator;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.google.gson.annotations.SerializedName;
import io.avaje.jsonb.Json.Alias;
import io.avaje.prism.GeneratePrism;

import javax.lang.model.element.Element;
import java.util.List;
import java.util.Optional;

@GeneratePrism(
  value = Alias.class,
  name = "AvajeAliasPrism",
  superInterfaces = AliasPrism.class)
@GeneratePrism(
  value = JsonAlias.class,
  name = "JacksonAliasPrism",
  superInterfaces = AliasPrism.class)
@GeneratePrism(
  value = SerializedName.class,
  name = "GsonAliasPrism")
public interface AliasPrism {

  static Optional<AliasPrism> getOptionalOn(Element element) {
    return Optional.<AliasPrism>empty()
        .or(() -> AvajeAliasPrism.getOptionalOn(element))
        .or(() -> JacksonAliasPrism.getOptionalOn(element))
        .or(() -> GsonAliasPrism.getOptionalOn(element)
                    .map(GsonAliasPrism::alternate)
                    .map(aliases -> (AliasPrism) () -> aliases)
                    .filter(prism -> !prism.value().isEmpty()));
  }

  List<String> value();
}
