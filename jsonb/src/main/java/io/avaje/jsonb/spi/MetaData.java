package io.avaje.jsonb.spi;

import java.lang.annotation.*;

/**
 * Hold bean dependency meta data intended for internal use by code generation (Java annotation processing).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MetaData {

  Class<?>[] value();

}
