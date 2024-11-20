package io.avaje.jsonb;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.MODULE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.avaje.jsonb.Json.Import.Imports;

/**
 * Marks a type for JSON support.
 *
 * <h3>Examples:</h3>
 *
 * <pre>{@code
 *
 *   @Json(naming = LowerHyphen)
 *   public class Customer ...
 *
 * }</pre>
 *
 * <pre>{@code
 *
 *   @Json
 *   public record Product( ... )
 *
 * }</pre>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Json {

  /**
   * Specify the naming convention to use for the properties on this type.
   * <p>
   * By default, the naming used is {@link Naming#Match}
   */
  Naming naming() default Naming.Match;

  /**
   * When {@code @Json.SubType} is used this specifies the name of the property
   * field that holds the type name (discriminator value).
   * <p>
   * This defaults to {@code @type} when unspecified.
   */
  String typeProperty() default "@type";

  /**
   * When set to true on deserialization keys are matched insensitive to case.
   */
  boolean caseInsensitiveKeys() default false;

  /**
   * Specify types to generate JsonAdapters for.
   *
   * <p>These types are typically in an external project / dependency or otherwise types that we
   * can't or don't want to explicitly annotate with {@code @Json}.
   *
   * <p>Typically, we put this annotation on a package.
   *
   * <pre>{@code
   * @Json.Import({Customer.class, Product.class, ...})
   * package org.example.processor;
   *
   * }</pre>
   */
  @Retention(SOURCE)
  @Repeatable(Imports.class)
  @Target({TYPE, PACKAGE, MODULE})
  @interface Import {

    /** Specify types to generate Json Adapters for. */
    Class<?>[] value();

    /** Specify the Json setting to apply to the imported classes */
    Json jsonSettings() default @Json;

    /** Specify the Subtype information. Can only be used if there is only one abstract type being imported */
    SubType[] subtypes() default {};

    /**
     * When importing an Interface or abstract type use this implementation for `fromJson()`.
     */
    Class<?> implementation() default Void.class;

    /**
     * The list of types to Import for Jsonb.
     */
    @Retention(SOURCE)
    @Target({TYPE, PACKAGE, MODULE})
    @interface Imports {

      Import[] value();
    }
  }

  /**
   * Override the json property name.
   *
   * <pre>{@code
   *
   *   @Json.Property("$code")
   *   String referenceCode;
   *
   * }</pre>
   */
  @Retention(SOURCE)
  @Target({FIELD, METHOD})
  @interface Property {

    /**
     * Specify the name for this property.
     */
    String value();
  }

  /**
   * Define one or more alternative names for a property accepted
   * during deserialization.
   *
   * <pre>{@code
   * @Json.Alias("$code")
   * String referenceCode;
   *
   * }</pre>
   */
  @Retention(SOURCE)
  @Target({FIELD, PARAMETER})
  @interface Alias {

    /** One or more secondary names to accept as aliases to the official name. */
    String[] value();
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
  @Target(FIELD)
  @Retention(SOURCE)
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
  @Retention(SOURCE)
  @Target({FIELD, METHOD, PARAMETER})
  @interface Unmapped {

  }

  /**
   * Mark a method on an Enum that provides the json value.
   *
   * <p>If the method returns an int type then it is mapped to json int, otherwise it is treated as
   * providing json string values.
   *
   * <pre>{@code
   * public enum MyEnum {
   *
   *   ONE("one value"),
   *   TWO("two value");
   *
   *   final String val;
   *   MyEnum(String val) {
   *     this.val = val;
   *   }
   *
   *   // method provides the values used to serialize to and from json
   *
   *   @Json.Value
   *   public String value() {
   *     return val;
   *   }
   * }
   *
   * }</pre>
   */
  @Target(METHOD)
  @Retention(SOURCE)
  @interface Value {}

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
  @Target(TYPE)
  @Retention(SOURCE)
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
  @Target(TYPE)
  @Retention(SOURCE)
  @interface SubTypes {

    SubType[] value();
  }

  /**
   * Marks a String field as containing raw JSON content.
   */
  @Retention(SOURCE)
  @Target({FIELD, METHOD})
  @interface Raw {

  }

  /**
   * Mark this Class as a MixIn Type that can add Jsonb Annotations on the specified type.
   * <p>
   * These types are typically in an external project / dependency or otherwise
   * types that we can't or don't want to explicitly annotate with {@code @Json}.
   * <p>
   * In the example below, the VehicleMixin class augments the the generated Vehicle JsonB
   * adapter to use "ford-type" as the json property.
   *
   * <pre>{@code
   *
   *   @Json.MixIn(Vehicle.class)
   *   public abstract class VehicleMixIn {
   *
   *   @Json.Property("ford-type")
   *   private String type;
   *    ...
   *
   * }</pre>
   */
  @Target(TYPE)
  @Retention(SOURCE)
  @interface MixIn {
    /** The concrete type to mix. */
    Class<?> value();
  }

  /**
   * Marker annotation that can be used to define constructors or factory methods as one to use
   * for instantiating  new instances of the associated class. Can be used in Mixin classes to
   * override an existing deserialization method.
   * <p>
   * The parameter names will be used as keys for deserialization instead of the field names.
   * <p>
   * <h3>Examples:</h3>
   *
   * <pre>{@code
   *
   *   @Json
   *   public class Kingfisher {
   *
   *     @Json.Creator
   *     public Kingfisher(String name) {
   *        ...
   *     }
   *   ...
   *
   * }</pre>
   *
   * <pre>{@code
   *
   *   @Json
   *   public record Product( ... ) {
   *
   *   @Json.Creator
   *   public static Product factory(String name){
   *      ...
   *   }
   *
   * }</pre>
   */
  @Retention(SOURCE)
  @Target({CONSTRUCTOR, METHOD})
  @interface Creator {}

  /**
   * Use a custom (de)serializer for this field. The custom serializer should be registered with {@link CustomAdapter} with the {@link CustomAdapter#global()} property set to false.
   *
   * <pre>{@code
   *
   *   @Json
   *   public class Example {
   *
   *   @Json.Serializer(CustomDateSerializer.class)
   *   private LocalDate type;
   *    ...
   *
   * }</pre>
   */
  @Retention(SOURCE)
  @Target({FIELD, METHOD})
  @interface Serializer {

    /**
     * The custom serializer to use with this property.
     */
    Class<? extends JsonAdapter<?>> value();
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
