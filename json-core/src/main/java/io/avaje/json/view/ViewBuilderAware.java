package io.avaje.json.view;

import java.lang.invoke.MethodHandle;

/**
 * A (non-scalar) JsonAdapter that is part of building json views.
 * <p>
 * JsonAdapters for complex types that have more than one property like
 * CustomerJsonAdapter, AddressJsonAdapter can participate in building
 * a json view that will include only some of their properties.
 */
public interface ViewBuilderAware {

  /**
   * Build view for the top level.
   */
  default void build(ViewBuilder builder) {
    build(builder, null, null);
  }

  /**
   * Build nested part of the view.
   */
  void build(ViewBuilder builder, String name, MethodHandle handle);

}
