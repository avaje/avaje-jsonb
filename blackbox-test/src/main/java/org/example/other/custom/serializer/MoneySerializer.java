package org.example.other.custom.serializer;

import java.math.BigDecimal;
import java.math.RoundingMode;

import io.avaje.json.JsonAdapter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;

@CustomAdapter(global = false)
public class MoneySerializer implements JsonAdapter<BigDecimal> {

  /** Constructor takes Jsonb, registered via AdapterBuilder */
  public MoneySerializer(Jsonb jsonb) {}

  @Override
  public BigDecimal fromJson(JsonReader reader) {
    return reader.readDecimal().setScale(2, RoundingMode.DOWN);
  }

  @Override
  public void toJson(JsonWriter writer, BigDecimal value) {
    writer.value(value.setScale(2, RoundingMode.DOWN));
  }
}
