package io.avaje.json.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public final class CoreTypes {

  private static final List<Factory> factories = List.of(BasicAdapters.FACTORY);
  private static final Map<Type, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();
  private static final NullPlaceholder NULL_PLACEHOLDER = new NullPlaceholder();

  /**
   * Return a core supported type adapter or null.
   * @param type The type to get the adapter for.
   * @return The JsonAdapter for the type or null.
   */
  @SuppressWarnings({"unchecked"})
  public static <T> JsonAdapter<T> create(Type type) {
    final var adapter = adapterCache.computeIfAbsent(type, CoreTypes::createAdapter);
    return adapter == NULL_PLACEHOLDER ? null : (JsonAdapter<T>)adapter;
  }

  /**
   * Create a JsonAdapter for an Array of the given element type.
   */
  @SuppressWarnings("unchecked")
  public static <T> JsonAdapter<T> createArray(Class<?> elementType) {
    final var adapter = (JsonAdapter<Object>) createAdapter(elementType);
    return createArray(elementType, adapter);
  }

  /**
   * Create a JsonAdapter for an Array of the given type.
   *
   * @param elementType The element type of the array.
   * @return The JsonAdapter for an Array of the given type.
   */
  @SuppressWarnings("unchecked")
  public static <T> JsonAdapter<T> createArray(Class<?> elementType, JsonAdapter<Object> adapter) {
    return (JsonAdapter<T>)ArrayAdapter.create(requireNonNull(elementType), requireNonNull(adapter));
  }

  /**
   * Return a JsonAdapter for {@code byte[]}.
   */
  public static JsonAdapter<byte[]> byteArray() {
    return ArrayAdapter.byteArray();
  }

  /**
   * Create a JsonAdapter for a Map with a given adapter for the values.
   * @param valueAdapter The JsonAdapter used for the values in the map.
   * @return The JsonAdapter for the map.
   */
  public static <V> JsonAdapter<Map<String, V>> createMap(JsonAdapter<V> valueAdapter) {
    return MapAdapter.create(valueAdapter);
  }

  /**
   * Create a JsonAdapter for a List with a given adapter for the elements.
   * @param elementAdapter The JsonAdapter used for the elements in the list.
   * @return The JsonAdapter for the list.
   */
  public static <V> JsonAdapter<List<V>> createList(JsonAdapter<V> elementAdapter) {
    return CollectionAdapter.createList(elementAdapter);
  }

  /**
   * Create a JsonAdapter for a Set with a given adapter for the elements.
   * @param elementAdapter The JsonAdapter used for the elements in the set.
   * @return The JsonAdapter for the set.
   */
  public static <V> JsonAdapter<Set<V>> createSet(JsonAdapter<V> elementAdapter) {
    return CollectionAdapter.createSet(elementAdapter);
  }

  private static JsonAdapter<?> createAdapter(Type type) {
    for (Factory factory : factories) {
      final var adapter = factory.create(type);
      if (adapter != null) {
        return adapter;
      }
    }
    return NULL_PLACEHOLDER;
  }

  interface Factory {
    JsonAdapter<?> create(Type type);
  }

  static final class NullPlaceholder implements JsonAdapter<Void> {

    @Override
    public void toJson(JsonWriter writer, Void value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Void fromJson(JsonReader reader) {
      throw new UnsupportedOperationException();
    }
  }
}
