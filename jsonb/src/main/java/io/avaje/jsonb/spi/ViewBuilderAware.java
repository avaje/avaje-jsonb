package io.avaje.jsonb.spi;

import java.lang.invoke.MethodHandle;

public interface ViewBuilderAware {

  /**
   * Build view for the top level.
   */
  default void build(ViewBuilder builder) throws NoSuchMethodException, IllegalAccessException {
    build(builder, null, null);
  }

  /**
   * Build nested part of the view.
   */
  void build(ViewBuilder builder, String name, MethodHandle handle) throws NoSuchMethodException, IllegalAccessException;

}
