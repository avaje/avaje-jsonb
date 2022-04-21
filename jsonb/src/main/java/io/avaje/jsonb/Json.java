package io.avaje.jsonb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a type for JSON support.
 */
@Retention(CLASS)
@Target(ElementType.TYPE)
public @interface Json {

  /**
   * Specify the naming convention to use for the properties on this type.
   */
  Naming naming() default Naming.Match;

  /**
   * When {@code @Json.SubType} is used this specifies the name of the property
   * field that holds the type name (discriminator value).
   * <p>
   * This defaults to {@code @type} when unspecified.
   */
  String typeProperty() default "";

  /**
   * Specify types to generate JsonAdapters for.
   * <p>
   * These types are typically in an external project / dependency or otherwise
   * types that we can't or don't want to explicitly annotate with {@code @Json}.
   */
  @Retention(CLASS)
  @Target({ElementType.TYPE, ElementType.PACKAGE})
  @interface Import {

    /**
     * Specify types to generate Json Adapters for.
     */
    Class<?>[] value();
  }

  /**
   * Override the json property name.
   */
  @Retention(CLASS)
  @Target({ElementType.FIELD})
  @interface Property {

    /**
     * Specify the name for this property.
     */
    String value();
  }

  /**
   * Exclude the property from serialization, deserialization or both.
   * <p>
   * We can explicitly use {@code deserialize=true} to include the property in
   * deserialization but not serialization. For example, we might do this on
   * a property that represents a secret like a password.
   * <p>
   * We can explicitly use {@code serialize=true} to include the property in
   * serialization but not deserialization.
   */
  @Retention(CLASS)
  @Target({ElementType.FIELD})
  @interface Ignore {

    /**
     * Set this explicitly to true to include in serialization.
     */
    boolean serialize() default false;

    /**
     * Set this explicitly to true to include in deserialization.
     */
    boolean deserialize() default false;
  }

  /**
   * Annotate a {@code Map<String,Object>} field to hold unmapped json properties.
   * <p>
   * When reading unknown properties from json content these are read and put into
   * this map. When writing json this map is included back into the content.
   *
   * <pre>{@code
   *
   *   @Json.Unmapped
   *   Map<String, Object> unmapped;
   *
   * }</pre>
   */
  @Retention(CLASS)
  @Target({ElementType.FIELD})
  @interface Unmapped {

  }

  /**
   * Mark a method on an Enum that provides the json value.
   * <p>
   * If the method returns an int type then it is mapped to json int, otherwise it is
   * treated as providing json string values.
   *
   * <pre>{@code
   *
   *   public enum MyEnum {
   *
   *     ONE("one value"),
   *     TWO("two value");
   *
   *     final String val;
   *     MyEnum(String val) {
   *       this.val = val;
   *     }
   *
   *     // method provides the values used to serialise to and from json
   *
   *     @Json.Value
   *     public String value() {
   *       return val;
   *     }
   *   }
   *
   * }</pre>
   */
  @Retention(RUNTIME)
  @Target({ElementType.METHOD})
  @interface Value {
  }

  /**
   * Specify the subtypes that a given type can be represented as.
   * <p>
   * This is used on an interface type, abstract type or type with inheritance
   * to indicate all the concrete subtypes that can represent the type.
   * <p>
   * In the example below the abstract Vehicle type has 2 concrete subtypes
   * of Car and Truck that can represent the type.
   *
   * <pre>{@code
   *
   *   @Json
   *   @Json.SubType(type = Car.class)
   *   @Json.SubType(type = Truck.class, name = "TRUCK")
   *   public abstract class Vehicle {
   *    ...
   *
   * }</pre>
   */
  @Retention(CLASS)
  @Target({ElementType.TYPE})
  @Repeatable(SubTypes.class)
  @interface SubType {

    /**
     * The concrete type that extends or implements the base type.
     */
    Class<?> type();

    /**
     * The name or "discriminator value" that is used to identify the type.
     * <p>
     * When unspecified this is the short name of the class.
     */
    String name() default "";
  }

  /**
   * Container of all the concrete SubType's that an interface type or abstract
   * type can be represented as.
   */
  @Retention(CLASS)
  @Target({ElementType.TYPE})
  @interface SubTypes {

    SubType[] value();
  }

  /**
   * Marks a String field as containing raw JSON content.
   */
  @Retention(CLASS)
  @Target({ElementType.FIELD})
  @interface Raw {

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
