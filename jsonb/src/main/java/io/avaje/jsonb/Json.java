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

}
