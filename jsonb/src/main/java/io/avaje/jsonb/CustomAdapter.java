package io.avaje.jsonb;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a type as a basic user-provided JsonAdapter to be registered automatically.
 *
 * <p> A custom adapter registered using this annotation must have a public constructor accepting a Jsonb instance (or a public static JsonAdapter.Factory FACTORY field for generic adapters), and must directly implement the JsonAdapter Interface.
 *
 * <h3>Example:</h3>
 *
 * <pre>{@code
 * @CustomAdapter
 * public class CustomJsonAdapter implements JsonAdapter<CustomType> {
 *
 *   private final JsonAdapter<String> stringJsonAdapter;
 *   private final PropertyNames names;
 *
 *   public CustomJsonAdapter(Jsonb jsonb) {
 *     // use the jsonb adapter method to get type serializers
 *     stringJsonAdapter = jsonb.adapter(String.class);
 *     // add serialization names
 *     jsonb.properties("prop1","prop2", ...);
 *   }
 * ...
 *
 * }</pre>
 *
 * <h3>Example of Generic Adapter:</h3>
 *
 * <pre>{@code
 *
 * @CustomAdapter
 * public class CustomJsonAdapter<T> implements JsonAdapter<GenericType<T>> {
 *
 *   private final JsonAdapter<T> genericTypeAdapter;
 *   private final PropertyNames names;
 *
 *   public static final JsonAdapter.Factory FACTORY = (type, jsonb) -> {
 *       if (Types.isGenericTypeOf(type, GenericType.class)) {
 *         return new CustomJsonAdapter<>(jsonb, Types.typeArguments(type))
 *       }
 *       return null;
 *     };
 *
 *   public CustomJsonAdapter(Jsonb jsonb, Type[] types) {
 *     //use the jsonb adapter method to get type serializers
 *     genericTypeAdapter = jsonb.adapter(types[0]);
 *     //add serialization names
 *     jsonb.properties("prop1","prop2", ...);
 *   }
 * ...
 *
 * }</pre>
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface CustomAdapter {

  /**
   * Whether this adapter should override existing adapters for a type. When disabled the only way
   * to use the adapter is via the {@link Json.Serializer} annotation
   */
  boolean global() default true;
}
