package io.avaje.jsonb.spi;

import io.avaje.jsonb.Jsonb;

/**
 * User defined class to configure the Jsonb Builder instance or register custom JsonAdapters on
 * startup.
 *
 * <p>These are service loaded when Jsonb starts. They can be specified in {@code
 * META-INF/services/io.avaje.jsonb.spi.JsonbExtension} or when using java module system via a
 * {@code provides} clause in module-info.
 */
@FunctionalInterface
public interface JsonbComponent extends JsonbExtension {

  /** Register JsonAdapters with the Builder. */
  void register(Jsonb.Builder builder);
}
