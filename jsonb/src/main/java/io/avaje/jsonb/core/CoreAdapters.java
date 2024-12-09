package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.core.CoreTypes;
import io.avaje.jsonb.AdapterFactory;

import java.lang.reflect.Type;

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
}
