package io.avaje.jsonb.spi;

import java.lang.annotation.*;

/**
 * For internal use, holds metadata on generated adapters for use by code generation (Java annotation processing).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MetaData {

  /**
   * The generated JsonAdapters.
   */
  Class<?>[] value();

}
