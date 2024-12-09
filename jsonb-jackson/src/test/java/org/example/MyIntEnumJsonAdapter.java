package org.example;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonDataException;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;

public final class MyIntEnumJsonAdapter implements JsonAdapter<MyIntEnum> {

  private static final Map<MyIntEnum, Integer> toValue = new EnumMap<>(MyIntEnum.class);
  private static final Map<Integer, MyIntEnum> toEnum = new HashMap<>();
  private final JsonAdapter<Integer> adapter;

  public MyIntEnumJsonAdapter(Jsonb jsonb) {
    this.adapter = jsonb.adapter(Integer.class);
    for(final var enumConst : MyIntEnum.values()) {
      final var val = enumConst.customJsonIntValue();
      toValue.put(enumConst, val);
      toEnum.put(val, enumConst);
    }
  }

  @Override
  public void toJson(JsonWriter writer, MyIntEnum value) {
    adapter.toJson(writer, toValue.get(value));
  }

  @Override
  public MyIntEnum fromJson(JsonReader reader) {
    final var value = adapter.fromJson(reader);
    final var enumConstant = toEnum.get(value);
    if (enumConstant == null)
      throw new JsonDataException("Unable to determine MyIntEnum enum value for " + value);
    return enumConstant;
  }
}
