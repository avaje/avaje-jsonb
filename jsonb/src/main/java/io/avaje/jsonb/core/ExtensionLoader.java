package io.avaje.jsonb.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import io.avaje.jsonb.spi.AdapterFactory;
import io.avaje.jsonb.spi.GeneratedComponent;
import io.avaje.jsonb.spi.JsonbCustomizer;
import io.avaje.jsonb.spi.JsonbExtension;

/** Load all the services using the common service interface. */
final class ExtensionLoader {

  private static final List<GeneratedComponent> generatedComponents = new ArrayList<>();
  private static final List<JsonbCustomizer> customizers = new ArrayList<>();
  private static Optional<AdapterFactory> adapterFactory = Optional.empty();

  static {
    for (var spi : ServiceLoader.load(JsonbExtension.class)) {
      if (spi instanceof GeneratedComponent) {
        generatedComponents.add((GeneratedComponent) spi);
      } else if (spi instanceof JsonbCustomizer) {
        customizers.add((JsonbCustomizer) spi);
      } else if (spi instanceof AdapterFactory) {
        adapterFactory = Optional.of((AdapterFactory) spi);
      }
    }
  }

  static List<GeneratedComponent> generatedComponents() {
    return generatedComponents;
  }

  static List<JsonbCustomizer> customizers() {
    return customizers;
  }

  static Optional<AdapterFactory> adapterFactory() {
    return adapterFactory;
  }
}
