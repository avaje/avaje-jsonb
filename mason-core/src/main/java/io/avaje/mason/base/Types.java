package io.avaje.mason.base;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class Types {

  private Types() {
    // hide
  }

  /**
   * Returns an array type whose elements are all instances of {@code componentType}.
   */
  public static GenericArrayType arrayOf(Type elementType) {
    return new Util.GenericArrayTypeImpl(elementType);
  }

  public static ParameterizedType listOf(Type elementType) {
    return newParameterizedType(List.class, elementType);
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType}. Use this
   * method if {@code rawType} is not enclosed in another type.
   */
  public static ParameterizedType newParameterizedType(Type rawType, Type... typeArguments) {
    if (typeArguments.length == 0) {
      throw new IllegalArgumentException("Missing type arguments for " + rawType);
    }
    return new Util.ParameterizedTypeImpl(null, rawType, typeArguments);
  }

}
