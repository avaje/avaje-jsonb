package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.core.CoreTypes;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.jsonb.Jsonb;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CoreAdapters {

  static final AdapterFactory FACTORY = (type, jsonb) -> CoreTypes.create(type);

  static final AdapterFactory ARRAY_FACTORY = (type, jsonb) -> {
    Type elementType = Util.arrayComponentType(type);
    if (elementType == null) return null;
    if (elementType == byte.class) return CoreTypes.byteArray();
    Class<?> elementClass = Util.rawType(elementType);
    JsonAdapter<Object> elementAdapter = jsonb.adapter(elementType);
    return CoreTypes.createArray(elementClass, elementAdapter).nullSafe();
  };

  static final AdapterFactory MAP_FACTORY = (type, jsonb) -> {
    final var rawType = Util.rawType(type);
    if (rawType != Map.class) {
      return null;
    }
    final var valueTypes = Util.mapValueTypes(type, rawType);
    if (valueTypes[0] != String.class) {
      return null;
    }
    JsonAdapter<Object> valueAdapter = jsonb.adapter(valueTypes[1]);
    return CoreTypes.createMap(valueAdapter);
  };

  static final AdapterFactory COLLECTION_FACTORY = (type, jsonb) -> {
    Class<?> rawType = Util.rawType(type);
    if (rawType == List.class || rawType == Collection.class) {
      return newListAdapter(type, jsonb).nullSafe();
    } else if (rawType == Set.class) {
      return newSetAdapter(type, jsonb).nullSafe();
    }
    return null;
  };

  private static <T> JsonAdapter<List<T>> newListAdapter(Type type, Jsonb jsonb) {
    Type elementType = Util.collectionElementType(type);
    JsonAdapter<T> elementAdapter = jsonb.adapter(elementType);
    return CoreTypes.createList(elementAdapter);
  }

  private static <T> JsonAdapter<Set<T>> newSetAdapter(Type type, Jsonb jsonb) {
    Type elementType = Util.collectionElementType(type);
    JsonAdapter<T> elementAdapter = jsonb.adapter(elementType);
    return CoreTypes.createSet(elementAdapter);
  }
}
