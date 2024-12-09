package io.avaje.json.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CoreTypes {

  private static final List<Factory> factories = List.of(BasicAdapters.FACTORY);
  private static final Map<Type, JsonAdapter<?>> adapterCache = new ConcurrentHashMap<>();

  private static final NullPlaceholder NULL_PLACEHOLDER = new NullPlaceholder();

  @SuppressWarnings({"unchecked"})
  public static <T> JsonAdapter<T> create(Type type) {
    final var adapter = adapterCache.computeIfAbsent(type, CoreTypes::createAdapter);
    return adapter == NULL_PLACEHOLDER ? null : (JsonAdapter<T>)adapter;
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
