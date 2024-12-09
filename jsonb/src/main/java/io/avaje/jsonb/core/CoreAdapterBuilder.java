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

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.AdapterFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Builds and caches the JsonAdapter adapters for DJsonb.
 */
final class CoreAdapterBuilder {

  private final DJsonb context;
  private final List<AdapterFactory> factories;
  private final ThreadLocal<LookupChain> lookupChainThreadLocal = new ThreadLocal<>();
  private final Map<Object, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  CoreAdapterBuilder(DJsonb context, List<AdapterFactory> userFactories, boolean mathAsString) {
    this.context = context;
    this.factories = new ArrayList<>();
    this.factories.addAll(userFactories);
    this.factories.add(CoreAdapters.FACTORY);
    this.factories.add(BasicTypeAdapters.FACTORY);
    this.factories.add(JavaTimeAdapters.FACTORY);
    this.factories.add(new MathAdapters(mathAsString));
    this.factories.add(CoreAdapters.COLLECTION_FACTORY);
    this.factories.add(CoreAdapters.MAP_FACTORY);
    this.factories.add(EnumMapAdapter.FACTORY);
    this.factories.add(CoreAdapters.ARRAY_FACTORY);
    this.factories.add(OptionalAdapters.FACTORY);
  }

  /**
   * Return the adapter from cache if exists else return null.
   */
  @SuppressWarnings("unchecked")
  <T> JsonAdapter<T> get(Object cacheKey) {
    return (JsonAdapter<T>) adapterCache.get(cacheKey);
  }

  /**
   * Build for the simple non-annotated type case.
   */
  <T> JsonAdapter<T> build(Type type) {
    return build(type, type);
  }

  /**
   * Build given type and annotations.
   */
  @SuppressWarnings("unchecked")
  <T> JsonAdapter<T> build(Type type, Object cacheKey) {
    LookupChain lookupChain = lookupChainThreadLocal.get();
    if (lookupChain == null) {
      lookupChain = new LookupChain();
      lookupChainThreadLocal.set(lookupChain);
    }

    boolean success = false;
    JsonAdapter<T> adapterFromCall = lookupChain.push(type, cacheKey);
    try {
      if (adapterFromCall != null) {
        return adapterFromCall;
      }
      // Ask each factory to create the JSON adapter.
      for (AdapterFactory factory : factories) {
        JsonAdapter<T> result = (JsonAdapter<T>) factory.create(type, context);
        if (result != null) {
          // Success! Notify the LookupChain so it is cached and can be used by re-entrant calls.
          lookupChain.adapterFound(result);
          success = true;
          return result;
        }
      }
      throw new IllegalArgumentException("No JsonAdapter for " + type + ". Perhaps needs @Json or @Json.Import?");
    } catch (IllegalArgumentException e) {
      throw lookupChain.exceptionWithLookupStack(e);
    } finally {
      lookupChain.pop(success);
    }
  }

  /**
   * A possibly-reentrant chain of lookups for JSON adapters.
   *
   * <p>We keep track of the current stack of lookups: we may start by looking up the JSON adapter
   * for Employee, re-enter looking for the JSON adapter of HomeAddress, and re-enter again looking
   * up the JSON adapter of PostalCode. If any of these lookups fail we can provide a stack trace
   * with all of the lookups.
   *
   * <p>Sometimes a JSON adapter factory depends on its own product; either directly or indirectly.
   * To make this work, we offer a JSON adapter stub while the final adapter is being computed. When
   * it is ready, we wire the stub to that finished adapter. This is necessary in self-referential
   * object models, such as an {@code Employee} class that has a {@code List<Employee>} field for an
   * organization's management hierarchy.
   *
   * <p>This class defers putting any JSON adapters in the cache until the topmost JSON adapter has
   * successfully been computed. That way we don't pollute the cache with incomplete stubs, or
   * adapters that may transitively depend on incomplete stubs.
   */
  @SuppressWarnings("unchecked")
  final class LookupChain {
    final List<Lookup<?>> callLookups = new ArrayList<>();
    final Deque<Lookup<?>> stack = new ArrayDeque<>();
    boolean exceptionAnnotated;

    /**
     * Returns a JSON adapter that was already created for this call, or null if this is the first
     * time in this call that the cache key has been requested in this call. This may return a
     * lookup that isn't yet ready if this lookup is reentrant.
     */
    <T> JsonAdapter<T> push(Type type, Object cacheKey) {
      // Try to find a lookup with the same key for the same call.
      for (Lookup<?> lookup : callLookups) {
        if (lookup.cacheKey.equals(cacheKey)) {
          Lookup<T> hit = (Lookup<T>) lookup;
          stack.add(hit);
          return hit.adapter != null ? hit.adapter : hit;
        }
      }

      // We might need to know about this cache key later in this call. Prepare for that.
      Lookup<Object> lookup = new Lookup<>(type, cacheKey);
      callLookups.add(lookup);
      stack.add(lookup);
      return null;
    }

    /**
     * Sets the adapter result of the current lookup.
     */
    <T> void adapterFound(JsonAdapter<T> result) {
      Lookup<T> currentLookup = (Lookup<T>) stack.getLast();
      currentLookup.adapter = result;
    }

    /**
     * Completes the current lookup by removing a stack frame.
     *
     * @param success true if the adapter cache should be populated if this is the topmost lookup.
     */
    void pop(boolean success) {
      stack.removeLast();
      if (!stack.isEmpty()) {
        return;
      }
      lookupChainThreadLocal.remove();
      if (success) {
        lock.lock();
        try {
          for (Lookup<?> lookup : callLookups) {
            JsonAdapter<?> replaced = adapterCache.put(lookup.cacheKey, lookup.adapter);
            if (replaced != null) {
              ((Lookup<Object>) lookup).adapter = (JsonAdapter<Object>) replaced;
              adapterCache.put(lookup.cacheKey, replaced);
            }
          }
        } finally {
          lock.unlock();
        }
      }
    }

    IllegalArgumentException exceptionWithLookupStack(IllegalArgumentException e) {
      // Don't add the lookup stack to more than one exception; the deepest is sufficient.
      if (exceptionAnnotated) {
        return e;
      }
      exceptionAnnotated = true;
      int size = stack.size();
      if (size == 1) {
        return e;
      }
      StringBuilder errorMessageBuilder = new StringBuilder(e.getMessage());
      for (Iterator<Lookup<?>> i = stack.descendingIterator(); i.hasNext(); ) {
        Lookup<?> lookup = i.next();
        errorMessageBuilder.append("\nfor ").append(lookup.type);
      }
      return new IllegalArgumentException(errorMessageBuilder.toString(), e);
    }
  }

  /**
   * This class implements {@code JsonAdapter} so it can be used as a stub for re-entrant calls.
   */
  static final class Lookup<T> implements JsonAdapter<T> {
    final Type type;
    final Object cacheKey;
    JsonAdapter<T> adapter;

    Lookup(Type type, Object cacheKey) {
      this.type = type;
      this.cacheKey = cacheKey;
    }

    @Override
    public T fromJson(JsonReader reader) {
      if (adapter == null) throw new IllegalStateException("JsonAdapter isn't ready");
      return adapter.fromJson(reader);
    }

    @Override
    public void toJson(JsonWriter writer, T value) {
      if (adapter == null) throw new IllegalStateException("JsonAdapter isn't ready");
      adapter.toJson(writer, value);
    }

    @Override
    public String toString() {
      return adapter != null ? adapter.toString() : super.toString();
    }
  }
}
