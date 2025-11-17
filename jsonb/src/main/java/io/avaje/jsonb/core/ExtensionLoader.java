package io.avaje.jsonb.core;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import io.avaje.jsonb.spi.GeneratedComponent;
import io.avaje.jsonb.spi.JsonStreamFactory;
import io.avaje.jsonb.spi.JsonbComponent;
import io.avaje.jsonb.spi.JsonbExtension;

/** Load all the services using the common service interface. */
final class ExtensionLoader {

  private static final List<GeneratedComponent> generatedComponents = new ArrayList<>();
  private static final List<JsonbComponent> userComponents = new ArrayList<>();
  private static Optional<JsonStreamFactory> adapterFactory = Optional.empty();
  private static final Lookup LOOKUP = MethodHandles.lookup();
  private static final HashMap<String, Lookup> LOOKUP_MAP = new HashMap<>();

  static void init(ClassLoader classLoader) {
    for (var spi : ServiceLoader.load(JsonbExtension.class, classLoader)) {
      if (spi instanceof GeneratedComponent) {
    	var gen =  (GeneratedComponent) spi;
        generatedComponents.add(gen);
        LOOKUP_MAP.computeIfAbsent(gen.getClass().getModule().getName(), k -> gen.lookup());
      } else if (spi instanceof JsonbComponent) {
        userComponents.add((JsonbComponent) spi);
      } else if (spi instanceof JsonStreamFactory) {
        adapterFactory = Optional.of((JsonStreamFactory) spi);
      }
    }
  }

  static List<GeneratedComponent> generatedComponents() {
    return generatedComponents;
  }

  static List<JsonbComponent> userComponents() {
    return userComponents;
  }

  static Optional<JsonStreamFactory> adapterFactory() {
    return adapterFactory;
  }

  static Lookup lookupLookup(Class<?> clazz) {
    return LOOKUP_MAP.getOrDefault(clazz.getModule().getName(), LOOKUP);
  }
}
