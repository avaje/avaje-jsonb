package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.core.CoreTypes;
import io.avaje.jsonb.AdapterFactory;

import java.lang.reflect.Type;
import java.util.Map;

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
}
