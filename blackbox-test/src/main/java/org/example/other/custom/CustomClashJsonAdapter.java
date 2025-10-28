package org.example.other.custom;

import java.lang.reflect.Type;
import java.util.List;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.AdapterFactory;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Json;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;

@CustomAdapter
public class CustomClashJsonAdapter<T>
    implements JsonAdapter<CustomClashJsonAdapter.NestedGeneric<T>> {

  public static final AdapterFactory FACTORY =
      (type, jsonb) -> {
        if (Types.isGenericTypeOf(type, NestedGeneric.class)) {
          return new CustomClashJsonAdapter<>(jsonb, Types.typeArguments(type));
        }
        return null;
      };

  public CustomClashJsonAdapter(Jsonb jsonb, Type[] types) {}

  public record NestedGeneric<T>(T value) {}

  @Json
  public record NestedGeneric2(List<NestedGeneric<String>> value) {}

  @Override
  public void toJson(JsonWriter writer, NestedGeneric<T> value) {}

  @Override
  public NestedGeneric<T> fromJson(JsonReader reader) {
    return null;
  }
}
