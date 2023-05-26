package io.avaje.jsonb;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

/**
 * Marks a type as a basic user-provided JsonAdapter to be registered automatically.
 *
 * <p> A custom adapter registered using this annotation must have a public constructor accepting a Jsonb instance, and must directly implement the JsonAdapter Interface.
 *
 * <h3>Example:</h3>
 *
 * <pre>{@code
 * @CustomAdapter
 * public class CustomJsonAdapter implements JsonAdapter<CustomType> {
 *   private final JsonAdapter<String> stringJsonAdapter;
 *   private final PropertyNames names;
 *
 *   public CustomJsonAdapter(Jsonb jsonb) {
 *     //use the jsonb adapter method to get type serializers
 *     stringJsonAdapter = jsonb.adapter(String.class);
 *     //add serialization names
 *     jsonb.properties("prop1","prop2", ...);
 *   }
 * ...
 *
 * }</pre>
 */
@Target(TYPE)
public @interface CustomAdapter {}
