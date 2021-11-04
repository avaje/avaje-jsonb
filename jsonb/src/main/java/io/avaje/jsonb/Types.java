package io.avaje.jsonb;


import io.avaje.jsonb.core.Util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

  public static Type listOf(Type elementType) {
    return Util.listOf(elementType);
  }

  public static Type setOf(Type elementType) {
    return Util.setOf(elementType);
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType}. Use this
   * method if {@code rawType} is not enclosed in another type.
   */
  public static ParameterizedType newParameterizedType(Type rawType, Type... typeArguments) {
    return Util.newParameterizedType(rawType, typeArguments);
  }

}
