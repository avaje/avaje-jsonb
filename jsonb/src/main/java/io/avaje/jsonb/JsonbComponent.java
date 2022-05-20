package io.avaje.jsonb;

/**
 * User defined components to register custom JsonAdapters with Jsonb.Builder.
 * <p>
 * These are service loaded when Jsonb starts. They can be specified in
 * {@code META-INF/services/io.avaje.jsonb.JsonbComponent} or when using
 * java module system via a {@code provides} clause in module-info.
 */
@FunctionalInterface
public interface JsonbComponent {

  /**
   * Register JsonAdapters with the Builder.
   */
  void register(Jsonb.Builder builder);
}
