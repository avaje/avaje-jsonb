/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.avaje.jsonb;

import io.avaje.jsonb.core.Util;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Factory methods for types.
 */
public final class Types {

  private Types() {
    // hide
  }

  public static <T> Collection<T> nullToEmpty(Collection<T> source) {
    return source == null ? Collections.emptyList() : source;
  }

  /**
   * Returns an array type whose elements are all instances of {@code componentType}.
   */
  public static GenericArrayType arrayOf(Type elementType) {
    return Util.arrayOf(elementType);
  }

  /**
   * Returns a Type that is a List of the given element type.
   */
  public static ParameterizedType listOf(Type elementType) {
    return newParameterizedType(List.class, elementType);
  }

  /**
   * Returns a Type that is a Set of the given element type.
   */
  public static ParameterizedType setOf(Type elementType) {
    return newParameterizedType(Set.class, elementType);
  }

  /**
   * Returns a Type that is a Stream of the given element type.
   */
  public static ParameterizedType streamOf(Type elementType) {
    return newParameterizedType(Stream.class, elementType);
  }

  /**
   * Return the Type for a Map with String keys and the given value element type.
   *
   * @param valueElementType The type of the values in the Map.
   * @return Type for a Map with String keys and the given value element type.
   */
  public static ParameterizedType mapOf(Type valueElementType) {
    return newParameterizedType(Map.class, String.class, valueElementType);
  }

  /**
   * Returns a Type that is an Optional of the given element type.
   */
  public static ParameterizedType optionalOf(Type valueElementType) {
    return newParameterizedType(Optional.class, valueElementType);
  }

  /**
   * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType}. Use this
   * method if {@code rawType} is not enclosed in another type.
   */
  public static ParameterizedType newParameterizedType(Type rawType, Type... typeArguments) {
    return Util.newParameterizedType(rawType, typeArguments);
  }

  /**
   * Return the raw type for the given potentially generic type.
   */
  public static Class<?> rawType(Type type) {
    if (type instanceof Class<?>) {
      // type is a normal class.
      return (Class<?>) type;

    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;

      // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
      // suspects some pathological case related to nested classes exists.
      Type rawType = parameterizedType.getRawType();
      return (Class<?>) rawType;

    } else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return Array.newInstance(rawType(componentType), 0).getClass();

    } else if (type instanceof TypeVariable) {
      // We could use the variable's bounds, but that won't work if there are multiple. having a raw
      // type that's more general than necessary is okay.
      return Object.class;

    } else if (type instanceof WildcardType) {
      return rawType(((WildcardType) type).getUpperBounds()[0]);

    } else {
      String className = type == null ? "null" : type.getClass().getName();
      throw new IllegalArgumentException(
        "Expected a Class, ParameterizedType, or  GenericArrayType, but <" + type + "> is of type " + className);
    }
  }

  /**
   * Return the generic type arguments expecting type to be a ParameterizedType.
   */
  public static Type[] typeArguments(Type type) {
    if (type instanceof ParameterizedType) {
      return ((ParameterizedType) type).getActualTypeArguments();
    }
    String className = type == null ? "null" : type.getClass().getName();
    throw new IllegalArgumentException("Expected ParameterizedType but <" + type + "> is of type " + className);
  }

  /** Helper method to determine if the given type can be handled by an adapter */
  public static boolean isGenericTypeOf(Type jsonType, Class<?> adapterClass) {
    return (jsonType instanceof GenericArrayType || jsonType instanceof ParameterizedType)
            && rawType(jsonType) == adapterClass;
  }
}
