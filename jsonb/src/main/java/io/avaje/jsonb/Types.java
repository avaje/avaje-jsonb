package io.avaje.jsonb;


import io.avaje.jsonb.core.Util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory methods for types.
 */
public class Types {

  private Types() {
    // hide
  }

  /**
   * Returns an array type whose elements are all instances of {@code componentType}.
   */
  public static Type arrayOf(Type elementType) {
    return Util.arrayOf(elementType);
  }

  /**
   * Returns a Type that is a List of the given element type.
   */
  public static Type listOf(Type elementType) {
    return newParameterizedType(List.class, elementType);
  }

  /**
   * Returns a Type that is a Set of the given element type.
   */
  public static Type setOf(Type elementType) {
    return newParameterizedType(Set.class, elementType);
  }

  /**
   * Return the Type for a Map with String keys and the given value element type.
   *
   * @param valueElementType The type of the values in the Map.
   * @return Type for a Map with String keys and the given value element type.
   */
  public static Type mapOf(Type valueElementType) {
    return newParameterizedType(Map.class, String.class, valueElementType);
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType}. Use this
   * method if {@code rawType} is not enclosed in another type.
   */
  public static ParameterizedType newParameterizedType(Type rawType, Type... typeArguments) {
    return Util.newParameterizedType(rawType, typeArguments);
  }

}
