package io.avaje.jsonb.spi;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

/** Component interface registers generated JsonAdapters to the Jsonb.Builder */
@FunctionalInterface
public interface GeneratedComponent extends JsonbComponent, JsonbExtension {

  /** Method handle Lookup for Json Views */
  default Lookup lookup() {
    return MethodHandles.lookup();
  }
}
