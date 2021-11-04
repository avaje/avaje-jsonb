package io.avaje.jsonb;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Marks a type for JSON support.
 */
@Retention(CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Json {

  /**
   * Specify the naming convention to use for the properties on this type.
   */
  Naming naming() default Naming.Match;

  /**
   * Specify types to generate JsonAdapters for.
   * <p>
   * These types are typically in an external project / dependency or otherwise
   * types that we can't or don't want to explicitly annotate with {@code @Json}.
   */
  @Retention(CLASS)
  @Target({ElementType.TYPE, ElementType.PACKAGE})
  @Documented
  @interface Import {

    /**
     * Specify types to generate Json Adapters for.
     */
    Class<?>[] value();
  }

  /**
   * The naming convention that we can use for a given type.
   */
  enum Naming {
    Match,
    LowerHyphen,
    LowerUnderscore,
    LowerSpace,
    UpperCamel,
    UpperHyphen,
    UpperUnderscore,
    UpperSpace
  }
}
