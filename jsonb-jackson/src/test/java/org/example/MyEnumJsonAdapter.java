package org.example;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.Generated;

@Generated
public final class MyEnumJsonAdapter implements JsonAdapter<MyEnum> {

  private static final Map<MyEnum, String> toValue = new EnumMap<>(MyEnum.class);
  private static final Map<String, MyEnum> toEnum = new HashMap<>();
  private final JsonAdapter<String> adapter;

  public MyEnumJsonAdapter(Jsonb jsonb) {
    this.adapter = jsonb.adapter(String.class);
    for (final var enumConst : MyEnum.values()) {
      final var val = enumConst.value();
      toValue.put(enumConst, val);
      toEnum.put(val, enumConst);
    }
  }

  @Override
  public void toJson(JsonWriter writer, MyEnum value) {
    adapter.toJson(writer, toValue.get(value));
  }

  @Override
  public MyEnum fromJson(JsonReader reader) {
    final var value = adapter.fromJson(reader);
    final var enumConstant = toEnum.get(value);
    if (enumConstant == null)
      throw new JsonDataException("Unable to determine MyEnum enum value for " + value);
    return enumConstant;
  }
}
