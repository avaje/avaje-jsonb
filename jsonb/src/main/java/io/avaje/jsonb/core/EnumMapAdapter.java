package io.avaje.jsonb.core;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.*;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

/** Converts maps with Enum keys to JSON objects. */
@SuppressWarnings("unchecked")
final class EnumMapAdapter<K extends Enum<K>, V> implements JsonAdapter<Map<K, V>> {

  static final AdapterFactory FACTORY =
      (type, jsonb) -> {
        final var rawType = Util.rawType(type);
        if (rawType != EnumMap.class && rawType != Map.class) {
          return null;
        }
        final var types = Util.mapValueTypes(type, rawType);
        if (!((Class<?>) types[0]).isEnum()) {
          return null;
        }
        return new EnumMapAdapter<>(jsonb, types).nullSafe();
      };
  private final Class<K> enumClass;
  private final JsonAdapter<V> valueAdapter;

  EnumMapAdapter(Jsonb jsonb, Type[] valueType) {
    this.enumClass = (Class<K>) valueType[0];
    this.valueAdapter = jsonb.adapter(valueType[1]);
  }

  @Override
  public void toJson(JsonWriter writer, Map<K, V> map) {
    writer.beginObject();
    for (final var entry : map.entrySet()) {
      if (entry.getKey() == null) {
        throw new JsonDataException("Map key is null at " + writer.path());
      }
      writer.name(entry.getKey().name());
      valueAdapter.toJson(writer, entry.getValue());
    }
    writer.endObject();
  }

  @Override
  public Map<K, V> fromJson(JsonReader reader) {
    final Map<K, V> result = new EnumMap<>(enumClass);
    reader.beginObject();
    while (reader.hasNextField()) {
      final String name = reader.nextField();
      final var enumVal = Enum.valueOf(enumClass, name);
      final V value = valueAdapter.fromJson(reader);
      final V replaced = result.put(enumVal, value);
      if (replaced != null) {
        throw new JsonDataException(
            String.format(
                "Map key '%s' has multiple values at path %s : %s and %s",
                name, reader.location(), replaced, value));
      }
    }
    reader.endObject();
    return result;
  }

  @Override
  public String toString() {
    return "EnumMapAdapter(" + enumClass + ", " + valueAdapter + ")";
  }
}
