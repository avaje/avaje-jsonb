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
