package org.example.other.custom.serializer;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@CustomAdapter(global = false)
public class MoneySerializer2 implements JsonAdapter<BigDecimal> {

  /**  Default constructor -> registered via {@code Supplier<JsonAdapter>} */
  public MoneySerializer2() { }

  @Override
  public BigDecimal fromJson(JsonReader reader) {
    return reader.readDecimal().setScale(2, RoundingMode.DOWN);
  }

  @Override
  public void toJson(JsonWriter writer, BigDecimal value) {
    writer.value(value.setScale(2, RoundingMode.DOWN));
  }
}
