package org.example.other.custom;

import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import io.avaje.jsonb.*;
import io.avaje.json.*;

@CustomAdapter
public class CustomEntryJsonAdapter<K, V> implements JsonAdapter<Entry<K, V>> {

  private final JsonAdapter<K> generic1;
  private final JsonAdapter<V> generic2;
  private final PropertyNames names;

  public static final AdapterFactory FACTORY =
      (type, jsonb) -> {
        if (Types.isGenericTypeOf(type, Entry.class)) {
          return new CustomEntryJsonAdapter<>(jsonb, Types.typeArguments(type));
        }
        return null;
      };

  public CustomEntryJsonAdapter(Jsonb jsonb, Type[] types) {
    this.generic1 = jsonb.adapter(types[0]);
    this.generic2 = jsonb.adapter(types[1]);
    this.names = jsonb.properties("key", "val");
  }

  @Override
  public void toJson(JsonWriter writer, Entry<K, V> value) {

    writer.beginObject(names);
    writer.name(0);
    generic1.toJson(writer, value.getKey());
    writer.name(1);
    generic2.toJson(writer, value.getValue());
    writer.endObject();
  }

  @Override
  public Entry<K, V> fromJson(JsonReader reader) {
    // variables to read json values into, constructor params don't need _set$ flags
    K key = null;
    V val = null;

    // read json
    reader.beginObject(names);
    while (reader.hasNextField()) {
      final String fieldName = reader.nextField();
      switch (fieldName) {
        case "key":
          key = generic1.fromJson(reader);
          break;
        case "val":
          val = generic2.fromJson(reader);
          break;

        default:
          reader.unmappedField(fieldName);
          reader.skipValue();
      }
    }
    reader.endObject();

    return new SimpleImmutableEntry<>(key, val);
  }
}
