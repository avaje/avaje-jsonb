package io.avaje.jsonb.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import io.avaje.jsonb.spi.AdapterFactory;
import io.avaje.jsonb.spi.GeneratedComponent;
import io.avaje.jsonb.spi.JsonbComponent;
import io.avaje.jsonb.spi.JsonbExtension;

/** Load all the services using the common service interface. */
final class ExtensionLoader {

  private static final List<GeneratedComponent> generatedComponents = new ArrayList<>();
  private static final List<JsonbComponent> userComponents = new ArrayList<>();
  private static Optional<AdapterFactory> adapterFactory = Optional.empty();

  static {
    for (var spi : ServiceLoader.load(JsonbExtension.class)) {
      if (spi instanceof GeneratedComponent) {
        generatedComponents.add((GeneratedComponent) spi);
      } else if (spi instanceof JsonbComponent) {
        userComponents.add((JsonbComponent) spi);
      } else if (spi instanceof AdapterFactory) {
        adapterFactory = Optional.of((AdapterFactory) spi);
      }
    }
  }

  static List<GeneratedComponent> generatedComponents() {
    return generatedComponents;
  }

  static List<JsonbComponent> userComponents() {
    return userComponents;
  }

  static Optional<AdapterFactory> adapterFactory() {
    return adapterFactory;
  }
}
