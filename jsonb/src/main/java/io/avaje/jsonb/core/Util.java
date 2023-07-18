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
package io.avaje.jsonb.core;

import io.avaje.jsonb.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Utility methods for defining Types.
 */
public final class Util {

  static final Type[] EMPTY_TYPE_ARRAY = {};

  private Util() {
  }

  /**
   * Returns an array type whose elements are all instances of {@code componentType}.
   */
  public static GenericArrayType arrayOf(Type elementType) {
    return new Util.GenericArrayTypeImpl(elementType);
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

  static boolean typesMatch(Type pattern, Type candidate) {
    return Util.equals(pattern, candidate);
  }

  static boolean isAnnotationPresent(Set<? extends Annotation> annotations, Class<? extends Annotation> annotationClass) {
    if (annotations.isEmpty()) return false; // Save an iterator in the common case.
    for (Annotation annotation : annotations) {
      if (annotation.annotationType() == annotationClass) return true;
    }
    return false;
  }

  static Type canonicalizeClass(Class<?> cls) {
    return cls.isArray() ? new GenericArrayTypeImpl(canonicalize(cls.getComponentType())) : cls;
  }

  /**
   * Returns a type that is functionally equal but not necessarily equal according to {@link
   * Object#equals(Object) Object.equals()}.
   */
  static Type canonicalize(Type type) {
    if (type instanceof Class) {
      Class<?> c = (Class<?>) type;
      return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;

    } else if (type instanceof ParameterizedType) {
      if (type instanceof ParameterizedTypeImpl) return type;
      ParameterizedType p = (ParameterizedType) type;
      return new ParameterizedTypeImpl(
        p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());

    } else if (type instanceof GenericArrayType) {
      if (type instanceof GenericArrayTypeImpl) return type;
      GenericArrayType g = (GenericArrayType) type;
      return new GenericArrayTypeImpl(g.getGenericComponentType());

    } else if (type instanceof WildcardType) {
      if (type instanceof WildcardTypeImpl) return type;
      WildcardType w = (WildcardType) type;
      return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());

    } else {
      return type; // This type is unsupported!
    }
  }

  /**
   * If type is a "? extends X" wildcard, returns X; otherwise returns type unchanged.
   */
  static Type removeSubtypeWildcard(Type type) {
    if (!(type instanceof WildcardType)) return type;

    Type[] lowerBounds = ((WildcardType) type).getLowerBounds();
    if (lowerBounds.length != 0) return type;

    Type[] upperBounds = ((WildcardType) type).getUpperBounds();
    if (upperBounds.length != 1) throw new IllegalArgumentException();

    return upperBounds[0];
  }

  static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
    return resolve(context, contextRawType, toResolve, new LinkedHashSet<TypeVariable<?>>());
  }

  private static Type resolve(
    Type context,
    Class<?> contextRawType,
    Type toResolve,
    Collection<TypeVariable<?>> visitedTypeVariables) {
    // This implementation is made a little more complicated in an attempt to avoid object-creation.
    while (true) {
      if (toResolve instanceof TypeVariable) {
        TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
        if (visitedTypeVariables.contains(typeVariable)) {
          // cannot reduce due to infinite recursion
          return toResolve;
        } else {
          visitedTypeVariables.add(typeVariable);
        }
        toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
        if (toResolve == typeVariable) return toResolve;

      } else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
        Class<?> original = (Class<?>) toResolve;
        Type componentType = original.getComponentType();
        Type newComponentType =
          resolve(context, contextRawType, componentType, visitedTypeVariables);
        return componentType == newComponentType ? original : arrayOf(newComponentType);

      } else if (toResolve instanceof GenericArrayType) {
        GenericArrayType original = (GenericArrayType) toResolve;
        Type componentType = original.getGenericComponentType();
        Type newComponentType =
          resolve(context, contextRawType, componentType, visitedTypeVariables);
        return componentType == newComponentType ? original : arrayOf(newComponentType);

      } else if (toResolve instanceof ParameterizedType) {
        ParameterizedType original = (ParameterizedType) toResolve;
        Type ownerType = original.getOwnerType();
        Type newOwnerType = resolve(context, contextRawType, ownerType, visitedTypeVariables);
        boolean changed = newOwnerType != ownerType;

        Type[] args = original.getActualTypeArguments();
        for (int t = 0, length = args.length; t < length; t++) {
          Type resolvedTypeArgument =
            resolve(context, contextRawType, args[t], visitedTypeVariables);
          if (resolvedTypeArgument != args[t]) {
            if (!changed) {
              args = args.clone();
              changed = true;
            }
            args[t] = resolvedTypeArgument;
          }
        }

        return changed
          ? new ParameterizedTypeImpl(newOwnerType, original.getRawType(), args)
          : original;

      } else if (toResolve instanceof WildcardType) {
        WildcardType original = (WildcardType) toResolve;
        Type[] originalLowerBound = original.getLowerBounds();
        Type[] originalUpperBound = original.getUpperBounds();

        if (originalLowerBound.length == 1) {
          Type lowerBound =
            resolve(context, contextRawType, originalLowerBound[0], visitedTypeVariables);
          if (lowerBound != originalLowerBound[0]) {
            return supertypeOf(lowerBound);
          }
        } else if (originalUpperBound.length == 1) {
          Type upperBound =
            resolve(context, contextRawType, originalUpperBound[0], visitedTypeVariables);
          if (upperBound != originalUpperBound[0]) {
            return subtypeOf(upperBound);
          }
        }
        return original;

      } else {
        return toResolve;
      }
    }
  }

  static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
    Class<?> declaredByRaw = declaringClassOf(unknown);

    // We can't reduce this further.
    if (declaredByRaw == null) return unknown;

    Type declaredBy = genericSupertype(context, contextRawType, declaredByRaw);
    if (declaredBy instanceof ParameterizedType) {
      int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
      return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
    }

    return unknown;
  }

  /**
   * Returns the generic supertype for {@code supertype}. For example, given a class {@code
   * IntegerSet}, the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the
   * result when the supertype is {@code Collection.class} is {@code Collection<Integer>}.
   */
  static Type genericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
    if (toResolve == rawType) {
      return context;
    }

    // we skip searching through interfaces if unknown is an interface
    if (toResolve.isInterface()) {
      Class<?>[] interfaces = rawType.getInterfaces();
      for (int i = 0, length = interfaces.length; i < length; i++) {
        if (interfaces[i] == toResolve) {
          return rawType.getGenericInterfaces()[i];
        } else if (toResolve.isAssignableFrom(interfaces[i])) {
          return genericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
        }
      }
    }

    // check our supertypes
    if (!rawType.isInterface()) {
      while (rawType != Object.class) {
        Class<?> rawSupertype = rawType.getSuperclass();
        if (rawSupertype == toResolve) {
          return rawType.getGenericSuperclass();
        } else if (toResolve.isAssignableFrom(rawSupertype)) {
          return genericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
        }
        rawType = rawSupertype;
      }
    }

    // we can't resolve this further
    return toResolve;
  }

  static int hashCodeOrZero(Object o) {
    return o != null ? o.hashCode() : 0;
  }

  static String typeToString(Type type) {
    return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
  }

  static int indexOf(Object[] array, Object toFind) {
    for (int i = 0; i < array.length; i++) {
      if (toFind.equals(array[i])) return i;
    }
    throw new NoSuchElementException();
  }

  /**
   * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
   * a class.
   */
  static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
    GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
    return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
  }

  static void checkNotPrimitive(Type type) {
    if ((type instanceof Class<?>) && ((Class<?>) type).isPrimitive()) {
      throw new IllegalArgumentException("Unexpected primitive " + type + ". Use the boxed type.");
    }
  }

  static final class ParameterizedTypeImpl implements ParameterizedType {
    private final Type ownerType;
    private final Type rawType;
    public final Type[] typeArguments;

    public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
      // Require an owner type if the raw type needs it.
      if (ownerType != null && rawType instanceof Class<?>) {
        Class<?> enclosingClass = ((Class<?>) rawType).getEnclosingClass();
        if (enclosingClass == null || Util.rawType(ownerType) != enclosingClass) {
          throw new IllegalArgumentException("unexpected owner type for " + rawType + ": " + ownerType);

        } else if (enclosingClass != null) {
          throw new IllegalArgumentException("unexpected owner type for " + rawType + ": null");
        }
      }

      this.ownerType = ownerType == null ? null : canonicalize(ownerType);
      this.rawType = canonicalize(rawType);
      this.typeArguments = typeArguments.clone();
      for (int t = 0; t < this.typeArguments.length; t++) {
        if (this.typeArguments[t] == null) throw new NullPointerException();
        checkNotPrimitive(this.typeArguments[t]);
        this.typeArguments[t] = canonicalize(this.typeArguments[t]);
      }
    }

    @Override
    public Type[] getActualTypeArguments() {
      return typeArguments.clone();
    }

    @Override
    public Type getRawType() {
      return rawType;
    }

    @Override
    public Type getOwnerType() {
      return ownerType;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof ParameterizedType && Util.equals(this, (ParameterizedType) other);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ hashCodeOrZero(ownerType);
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder(30 * (typeArguments.length + 1));
      result.append(typeToString(rawType));

      if (typeArguments.length == 0) {
        return result.toString();
      }

      result.append("<").append(typeToString(typeArguments[0]));
      for (int i = 1; i < typeArguments.length; i++) {
        result.append(", ").append(typeToString(typeArguments[i]));
      }
      return result.append(">").toString();
    }
  }

  static final class GenericArrayTypeImpl implements GenericArrayType {
    private final Type componentType;

    GenericArrayTypeImpl(Type componentType) {
      this.componentType = canonicalize(componentType);
    }

    @Override
    public Type getGenericComponentType() {
      return componentType;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof GenericArrayType && Util.equals(this, (GenericArrayType) o);
    }

    @Override
    public int hashCode() {
      return componentType.hashCode();
    }

    @Override
    public String toString() {
      return typeToString(componentType) + "[]";
    }
  }

  /**
   * The WildcardType interface supports multiple upper bounds and multiple lower bounds. We only
   * support what the Java 6 language needs - at most one bound. If a lower bound is set, the upper
   * bound must be Object.class.
   */
  static final class WildcardTypeImpl implements WildcardType {
    private final Type upperBound;
    private final Type lowerBound;

    WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
      if (lowerBounds.length > 1) throw new IllegalArgumentException();
      if (upperBounds.length != 1) throw new IllegalArgumentException();

      if (lowerBounds.length == 1) {
        if (lowerBounds[0] == null) throw new NullPointerException();
        checkNotPrimitive(lowerBounds[0]);
        if (upperBounds[0] != Object.class) throw new IllegalArgumentException();
        this.lowerBound = canonicalize(lowerBounds[0]);
        this.upperBound = Object.class;

      } else {
        if (upperBounds[0] == null) throw new NullPointerException();
        checkNotPrimitive(upperBounds[0]);
        this.lowerBound = null;
        this.upperBound = canonicalize(upperBounds[0]);
      }
    }

    @Override
    public Type[] getUpperBounds() {
      return new Type[]{upperBound};
    }

    @Override
    public Type[] getLowerBounds() {
      return lowerBound != null ? new Type[]{lowerBound} : EMPTY_TYPE_ARRAY;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof WildcardType && Util.equals(this, (WildcardType) other);
    }

    @Override
    public int hashCode() {
      // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
      return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
    }

    @Override
    public String toString() {
      if (lowerBound != null) {
        return "? super " + typeToString(lowerBound);
      } else if (upperBound == Object.class) {
        return "?";
      } else {
        return "? extends " + typeToString(upperBound);
      }
    }
  }

  static String typeAnnotatedWithAnnotations(Type type, Set<? extends Annotation> annotations) {
    return type + (annotations.isEmpty() ? " (with no annotations)" : " annotated " + annotations);
  }

  /**
   * Returns a type that represents an unknown type that extends {@code bound}. For example, if
   * {@code bound} is {@code CharSequence.class}, this returns {@code ? extends CharSequence}. If
   * {@code bound} is {@code Object.class}, this returns {@code ?}, which is shorthand for {@code ?
   * extends Object}.
   */
  static WildcardType subtypeOf(Type bound) {
    Type[] upperBounds;
    if (bound instanceof WildcardType) {
      upperBounds = ((WildcardType) bound).getUpperBounds();
    } else {
      upperBounds = new Type[]{bound};
    }
    return new Util.WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
  }

  /**
   * Returns a type that represents an unknown supertype of {@code bound}. For example, if {@code
   * bound} is {@code String.class}, this returns {@code ? super String}.
   */
  static WildcardType supertypeOf(Type bound) {
    Type[] lowerBounds;
    if (bound instanceof WildcardType) {
      lowerBounds = ((WildcardType) bound).getLowerBounds();
    } else {
      lowerBounds = new Type[]{bound};
    }
    return new Util.WildcardTypeImpl(new Type[]{Object.class}, lowerBounds);
  }


  static Class<?> rawType(Type type) {
    return Types.rawType(type);
  }

  /**
   * Returns the element type of this collection type.
   *
   * @throws IllegalArgumentException if this type is not a collection.
   */
  static Type collectionElementType(Type context) {
    Type collectionType = supertype(context, Collection.class, Collection.class);
    if (collectionType instanceof WildcardType) {
      collectionType = ((WildcardType) collectionType).getUpperBounds()[0];
    }
    if (collectionType instanceof ParameterizedType) {
      return ((ParameterizedType) collectionType).getActualTypeArguments()[0];
    }
    return Object.class;
  }

  /**
   * Returns true if {@code a} and {@code b} are equal.
   */
  static boolean equals(Type a, Type b) {
    if (a == b) {
      return true; // Also handles (a == null && b == null).

    } else if (a instanceof Class) {
      if (b instanceof GenericArrayType) {
        return equals(((Class<?>) a).getComponentType(), ((GenericArrayType) b).getGenericComponentType());
      }
      return a.equals(b); // Class already specifies equals().

    } else if (a instanceof ParameterizedType) {
      if (!(b instanceof ParameterizedType)) return false;
      ParameterizedType pa = (ParameterizedType) a;
      ParameterizedType pb = (ParameterizedType) b;
      Type[] aTypeArguments =
        pa instanceof Util.ParameterizedTypeImpl
          ? ((Util.ParameterizedTypeImpl) pa).typeArguments
          : pa.getActualTypeArguments();
      Type[] bTypeArguments =
        pb instanceof Util.ParameterizedTypeImpl
          ? ((Util.ParameterizedTypeImpl) pb).typeArguments
          : pb.getActualTypeArguments();
      return equals(pa.getOwnerType(), pb.getOwnerType())
        && pa.getRawType().equals(pb.getRawType())
        && Arrays.equals(aTypeArguments, bTypeArguments);

    } else if (a instanceof GenericArrayType) {
      if (b instanceof Class) {
        return equals(((Class<?>) b).getComponentType(), ((GenericArrayType) a).getGenericComponentType());
      }
      if (!(b instanceof GenericArrayType)) return false;
      GenericArrayType ga = (GenericArrayType) a;
      GenericArrayType gb = (GenericArrayType) b;
      return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

    } else if (a instanceof WildcardType) {
      if (!(b instanceof WildcardType)) return false;
      WildcardType wa = (WildcardType) a;
      WildcardType wb = (WildcardType) b;
      return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
        && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

    } else if (a instanceof TypeVariable) {
      if (!(b instanceof TypeVariable)) return false;
      TypeVariable<?> va = (TypeVariable<?>) a;
      TypeVariable<?> vb = (TypeVariable<?>) b;
      return va.getGenericDeclaration() == vb.getGenericDeclaration()
        && va.getName().equals(vb.getName());

    } else {
      // This isn't a supported type.
      return false;
    }
  }

  /**
   * Returns a two element array containing this map's key and value types in positions 0 and 1
   * respectively.
   */
  static Type[] mapValueTypes(Type context, Class<?> contextRawType) {
    // Work around a problem with the declaration of java.util.Properties. That class should extend
    // Hashtable<String, String>, but it's declared to extend Hashtable<Object, Object>.
    if (context == Properties.class) {
      return new Type[]{String.class,String.class};
    }
    Type mapType = supertype(context, contextRawType, Map.class);
    if (mapType instanceof ParameterizedType) {
      ParameterizedType mapParameterizedType = (ParameterizedType) mapType;
      return mapParameterizedType.getActualTypeArguments();
    }
    return new Type[] {String.class, Object.class};
  }

  /**
   * Returns the generic form of {@code supertype}. For example, if this is {@code
   * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
   * Iterable.class}.
   *
   * @param supertype a superclass of, or interface implemented by, this.
   */
  static Type supertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    if (!supertype.isAssignableFrom(contextRawType)) throw new IllegalArgumentException();
    return resolve(context, contextRawType, genericSupertype(context, contextRawType, supertype));
  }

  /**
   * Returns the element type of {@code type} if it is an array type, or null if it is not an array
   * type.
   */
  static Type arrayComponentType(Type type) {
    if (type instanceof GenericArrayType) {
      return ((GenericArrayType) type).getGenericComponentType();
    } else if (type instanceof Class) {
      return ((Class<?>) type).getComponentType();
    } else {
      return null;
    }
  }
}
