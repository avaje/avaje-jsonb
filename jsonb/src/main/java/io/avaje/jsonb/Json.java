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

  Naming naming() default Naming.Match;

  @Retention(CLASS)
  @Target({ElementType.TYPE, ElementType.PACKAGE})
  @Documented
  @interface Import {

    /**
     * Specify types to generate Json Adapters for.
     */
    Class<?>[] value();
  }

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
